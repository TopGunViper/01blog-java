Reactor模型是典型的事件驱动模型。在网络编程中，所谓的事件当然就是read、write、bind、connect、close等这些动作了。Reactor模型的实现有很多种，下面介绍最基本的三种：
- 单线程版
- 多线程版
- 主从多线程版

###### Key Word：Java NIO，Reactor模型，Java并发编程，Event-Driven

### 单线程版本

结构图（引用自[Doug Lea](http://gee.cs.oswego.edu/)的Scalable IO in Java）如下：
![Reactor模型图-来自](http://ob7uytere.bkt.clouddn.com/timestamp_1474469089432_test.png)

上图中Reactor是一个典型的事件驱动中心，客户端发起请求并建立连接时，会触发注册在多路复用器Selector上的SelectionKey.OP_ACCEPT事件，绑定在该事件上的Acceptor对象的职责就是接受请求，为接下来的读写操作做准备。

Reactor设计如下：
```
/**
 * Reactor
 * 
 * @author wqx
 *
 */
public class Reactor implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(Reactor.class);
	
	private Selector selector;
	
	private ServerSocketChannel ssc;

	private Handler DEFAULT_HANDLER = new Handler(){
		@Override
		public void processRequest(Processor processor, ByteBuffer msg) {
			//NOOP
		}
	};
	private Handler handler = DEFAULT_HANDLER;
	
	
	/**
	 * 启动阶段
	 * @param port
	 * @throws IOException
	 */
	public Reactor(int port, int maxClients, Handler serverHandler) throws IOException{
		selector = Selector.open();
		ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.socket().bind(new InetSocketAddress(port));
		
		this.handler = serverHandler;
		SelectionKey sk = ssc.register(selector, SelectionKey.OP_ACCEPT);
		sk.attach(new Acceptor());
	}
	/**
	 * 轮询阶段
	 */
	@Override
	public void run() {
		while(!ssc.socket().isClosed()){
			try {
				selector.select(1000);
				Set<SelectionKey> keys;
				synchronized(this){
					keys = selector.selectedKeys();
				}
				Iterator<SelectionKey> it = keys.iterator();
				while(it.hasNext()){
					SelectionKey key = it.next();
					dispatch(key);
					it.remove();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        close();
	}
	
	public void dispatch(SelectionKey key){
		Runnable r = (Runnable)key.attachment();
		if(r != null)
			r.run();
	}
	/**
	 * 用于接受TCP连接的Acceptor
	 * 
	 */
	class Acceptor implements Runnable{

		@Override
		public void run() {
			SocketChannel sc;
			try {
				sc = ssc.accept();
				if(sc != null){
					new Processor(Reactor.this,selector,sc);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void close(){
		try {
			selector.close();
			if(LOG.isDebugEnabled()){
				LOG.debug("Close selector");
			}
		} catch (IOException e) {
			LOG.warn("Ignoring exception during close selector, e=" + e);
		}
	}
	public void processRequest(Processor processor, ByteBuffer msg){
		if(handler != DEFAULT_HANDLER){
			handler.processRequest(processor, msg);
		}
	}
}

```
上面是典型的单线程版本的Reactor实现，实例化Reactor对象的过程中，在当前多路复用器Selector上注册了OP_ACCEPT事件，当OP_ACCEPT事件发生后，Reactor通过dispatch方法执行Acceptor的run方法，Acceptor类的主要功能就是接受请求，建立连接，并将代表连接建立的SocketChannel以参数的形式构造Processor对象。

> Processor的任务就是进行I/O操作。

下面是Processor的源码：
```
/**
 * Server Processor
 * 
 * @author wqx
 */
public class Processor implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(Processor.class);

	Reactor reactor;

	private SocketChannel sc;

	private final SelectionKey sk;

	private final ByteBuffer lenBuffer = ByteBuffer.allocate(4);

	private ByteBuffer inputBuffer = lenBuffer;

	private ByteBuffer outputDirectBuffer = ByteBuffer.allocateDirect(1024 * 64);

	private LinkedBlockingQueue<ByteBuffer> outputQueue = new LinkedBlockingQueue<ByteBuffer>();

	public Processor(Reactor reactor, Selector sel,SocketChannel channel) throws IOException{
		this.reactor = reactor;
		sc = channel;
		sc.configureBlocking(false);
		sk = sc.register(sel, SelectionKey.OP_READ);
		sk.attach(this);
		sel.wakeup();
	}

	@Override
	public void run() {
		if(sc.isOpen() && sk.isValid()){
			if(sk.isReadable()){
				doRead();
			}else if(sk.isWritable()){
				doSend();
			}
		}else{
			LOG.error("try to do read/write operation on null socket");
			try {
				if(sc != null)
					sc.close();
			} catch (IOException e) {}
		}
	}
	private void doRead(){
		try {
			int byteSize = sc.read(inputBuffer);
			
			if(byteSize < 0){
				LOG.error("Unable to read additional data");
			}
			if(!inputBuffer.hasRemaining()){
				
				if(inputBuffer == lenBuffer){
					//read length
					inputBuffer.flip();
					int len = inputBuffer.getInt();
					if(len < 0){
						throw new IllegalArgumentException("Illegal data length");
					}
					//prepare for receiving data
					inputBuffer = ByteBuffer.allocate(len);
				}else{
					//read data
					if(inputBuffer.hasRemaining()){
						sc.read(inputBuffer);
					}
					if(!inputBuffer.hasRemaining()){
						inputBuffer.flip();
						processRequest();
						//clear lenBuffer and waiting for next reading operation 
						lenBuffer.clear();
						inputBuffer = lenBuffer;
					}
				}
			}
		} catch (IOException e) {
			LOG.error("Unexcepted Exception during read. e=" + e);
			try {
				if(sc != null)
					sc.close();
			} catch (IOException e1) {
				LOG.warn("Ignoring exception when close socketChannel");
			}
		}
	}

	/**
	 * process request and get response
	 * 
	 * @param request
	 * @return
	 */
	private void processRequest(){
		reactor.processRequest(this,inputBuffer);
	}
	private void doSend(){
		try{
			/**
			 * write data to channel：
			 * step 1: write the length of data(occupy 4 byte)
			 * step 2: data content
			 */
			if(outputQueue.size() > 0){
				ByteBuffer directBuffer = outputDirectBuffer;
				directBuffer.clear();
				
				for(ByteBuffer buf : outputQueue){
					buf.flip();
					
					if(buf.remaining() > directBuffer.remaining()){
						//prevent BufferOverflowException
						buf = (ByteBuffer) buf.slice().limit(directBuffer.remaining());
					}
					//transfers the bytes remaining in buf into  directBuffer
					int p = buf.position();
					directBuffer.put(buf);
					//reset position
					buf.position(p);

					if(!directBuffer.hasRemaining()){
						break;
					}
				}
				directBuffer.flip();
				int sendSize = sc.write(directBuffer);
				
				while(!outputQueue.isEmpty()){
					ByteBuffer buf = outputQueue.peek();
					int left = buf.remaining() - sendSize;
					if(left > 0){
						buf.position(buf.position() + sendSize);
						break;
					}
					sendSize -= buf.remaining();
					outputQueue.remove();
				}
			}
			synchronized(reactor){
				if(outputQueue.size() == 0){
					//disable write
					disableWrite();
				}else{
					//enable write
					enableWrite();
				}
			}
		} catch (CancelledKeyException e) {
            LOG.warn("CancelledKeyException occur e=" + e);
        } catch (IOException e) {
            LOG.warn("Exception causing close, due to " + e);
        }
	}
	public void sendBuffer(ByteBuffer bb){
		try{
			synchronized(this.reactor){
				if(LOG.isDebugEnabled()){
					LOG.debug("add sendable bytebuffer into outputQueue");
				}
				//wrap ByteBuffer with length header
				ByteBuffer wrapped = wrap(bb);
				
				outputQueue.add(wrapped);
				
				enableWrite();
			}
		}catch(Exception e){
			LOG.error("Unexcepted Exception: ", e);
		}
	}
	
	private ByteBuffer wrap(ByteBuffer bb){
		bb.flip();
		lenBuffer.clear();
		int len = bb.remaining();
		lenBuffer.putInt(len);
		ByteBuffer resp = ByteBuffer.allocate(len+4);
		lenBuffer.flip();
		
		resp.put(lenBuffer);
		resp.put(bb);
		return resp;
	}
	private void enableWrite(){
		int i = sk.interestOps();
		if((i & SelectionKey.OP_WRITE) == 0){
			sk.interestOps(i | SelectionKey.OP_WRITE);
		}
	}
	private void disableWrite(){
		int i = sk.interestOps();
		if((i & SelectionKey.OP_WRITE) == 1){
			sk.interestOps(i & (~SelectionKey.OP_WRITE));			
		}
	}
}

```
其实Processor要做的事情很简单，就是向selector注册感兴趣的读写时间，OP_READ或OP_WRITE，然后等待事件触发，做相应的操作。
```
	@Override
	public void run() {
		if(sc.isOpen() && sk.isValid()){
			if(sk.isReadable()){
				doRead();
			}else if(sk.isWritable()){
				doSend();
			}
		}else{
			LOG.error("try to do read/write operation on null socket");
			try {
				if(sc != null)
					sc.close();
			} catch (IOException e) {}
		}
	}
```
而doRead()和doSend()方法稍微复杂了一点，这里其实处理了用TCP协议进行通信时必须要解决的问题：**TCP粘包拆包问题**

###### TCP粘包拆包问题
我们都知道TCP协议是面向字节流的，而字节流是连续的，无法有效识别应用层数据的边界。如下图：

![粘包拆包示意图](http://ob7uytere.bkt.clouddn.com/timestamp_1479800845597_test.png)
上图显示的应用层有三个数据包，D1，D2，D3.当应用层数据传到传输层后，可能会出现粘包拆包现象。

> TCP协议的基本传输单位是报文段，而每个报文段最大有效载荷是有限制的,一般以太网MTU为1500，去除IP头20B，TCP头20B，那么剩下的1460B就是传输层最大报文段的有效载荷。如果应用层数据大于该值（如上图中的数据块D2），那么传输层就会进行拆分重组。

**解决方案**
1. 消息定长（通信双方发送的消息固定长度，缺点很明显：浪费可耻！！！）
2. 每个消息之间加分割符（缺点：消息编解码耗时，并且如果消息体中本省就包含分隔字符，需要进行转义，效率低）
3. 每个数据包加个Header！！！（header中指定后面数据的长度，这不就是Tcp、Ip协议通用的做法么。。。哈哈）

**采用方案三**

示意图如下：
![timestamp_1479519802200_test.png](http://ob7uytere.bkt.clouddn.com/timestamp_1479519802200_test.png)

header区占用4B，内容为数据的长度。too simple。。。-_-

###### 理论有了，下面具体分析下Read、Write的实现过程：

**doRead**
inputBuffer负责接受数据，lenBuffer负责接受数据长度，初始化的时候，将lenBuffer赋给inputBuffer，定义如下：
```
private final ByteBuffer lenBuffer = ByteBuffer.allocate(4);
private ByteBuffer inputBuffer = lenBuffer;
```
1. 如果inputBuffer == lenBuffer,那么从inputBuffer中读取出一个整型值len，这个值就是接下来要接受的数据的长度。同时分配一个大小为len的内存空间，并复制给inputBuffer，准备接受数据！！！
```
	private void doRead(){
		try {
			int byteSize = sc.read(inputBuffer);
			
			if(byteSize < 0){
				LOG.error("Unable to read additional data");
			}
			if(!inputBuffer.hasRemaining()){
				
				if(inputBuffer == lenBuffer){
					//read length
					inputBuffer.flip();
					int len = inputBuffer.getInt();
					if(len < 0){
						throw new IllegalArgumentException("Illegal data length");
					}
					//prepare for receiving data
					inputBuffer = ByteBuffer.allocate(len);
                else{...}
```

2. 如果inputBuffer ！= lenBuffer，那么开始接受数据吧！
```
if(inputBuffer == lenBuffer){
        //。。。
}else{
	//read data
	if(inputBuffer.hasRemaining()){
		sc.read(inputBuffer);
	}
	if(!inputBuffer.hasRemaining()){
		inputBuffer.flip();
		processRequest();
		//clear lenBuffer and waiting for next reading operation 
		lenBuffer.clear();
		inputBuffer = lenBuffer;
	}
}
```
**注意**：
1. 必须保证缓冲区是满的，即inputBuffer.hasRemaining()=false
2. processRequest后，将inputBuffer重新赋值为lenBuffer，为下一次读操作做准备。

**doWrite**

用户调用sendBuffer方法发送数据，其实就是将数据加入outputQueue，这个outputQueue就是一个发送缓冲队列。
```
public void sendBuffer(ByteBuffer bb){
		try{
			synchronized(this.reactor){
				if(LOG.isDebugEnabled()){
					LOG.debug("add sendable bytebuffer into outputQueue");
				}
				//wrap ByteBuffer with length header
				ByteBuffer wrapped = wrap(bb);
				
				outputQueue.add(wrapped);
				
				enableWrite();
			}
		}catch(Exception e){
			LOG.error("Unexcepted Exception: ", e);
		}
	}
```
doSend方法就很好理解了，无非就是不断从outputQueue中取数据，然后写入channel中即可。过程如下：

将发送队列outputQueue中的数据写入缓冲区outputDirectBuffer：
1. 清空outputDirectBuffer，为发送数据做准备
2. 将outputQueue数据写入outputDirectBuffer
3. 调用socketChannel.write(outputDirectBuffer);将outputDirectBuffer写入socket缓冲区

> **执行步骤2的时候，我们可能会遇到这么几种情况：**
>> 1.某个数据包大小超过了outputDirectBuffer剩余空间大小
> 
>> 2.outputDirectBuffer已被填满，但是outputQueue仍有待发送的数据

> **执行步骤3的时候，也可能出现下面两种情况：**
>> 1.outputDirectBuffer被全部写入socket缓冲区
>
>> 2.outputDirectBuffer只有部分数据或者压根就没有数据被写入socket缓冲区

实现过程可以结合源码，这里重点分析下面几个点：

> 为什么需要重置buf的position
```
int p = buf.position();
directBuffer.put(buf);
//reset position
buf.position(p);
```

写入directBuffer的数据是即将被写入SocketChannel的数据，问题就在于：当我们调用
```
int sendSize = sc.write(directBuffer);
```
的时候，directBuffer中的数据都被写入Channel了吗？明显是不确定的（具体可以看java.nio.channels.SocketChannel.write(ByteBuffer src)的doc文档）

> 上面的问题如何解决

思路很简单，根据write方法返回值sendSize，遍历outputQueue中的ByteBuffer，根据buf.remaining()和sendSize的大小，才可以确定buf是否真的被发送了。如下所示：
```
while(!outputQueue.isEmpty()){
	ByteBuffer buf = outputQueue.peek();
	int left = buf.remaining() - sendSize;
	if(left > 0){
		buf.position(buf.position() + sendSize);
		break;
	}
	sendSize -= buf.remaining();
	outputQueue.remove();
}
```
网络通信基本解决，上面的处理思路是参照Zookeeper网络模块的实现，有兴趣可以看Zookeeper相应源码。

#### 总结
在这种实现方式中，dispatch方法是同步阻塞的！！！所有的IO操作和业务逻辑处理都在NIO线程（即Reactor线程）中完成。如果业务处理很快，那么这种实现方式没什么问题，不用切换到用户线程。但是，想象一下如果业务处理很耗时（涉及很多数据库操作、磁盘操作等），那么这种情况下Reactor将被阻塞，这肯定是我们不希望看到的。解决方法很简单，业务逻辑进行异步处理,即交给用户线程处理。

下面分析下单线程版的Reactor模型的缺点：
- 自始自终都只有一个Reactor线程，缺点很明显：Reactor意外挂了，整个系统也就无法正常工作，可靠性太差。
- 单线程的另外一个问题是在大负载的情况下，Reactor的处理速度必然会成为系统性能的瓶颈。


