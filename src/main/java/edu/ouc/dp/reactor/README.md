Reactorģ���ǵ��͵��¼�����ģ�͡����������У���ν���¼���Ȼ����read��write��bind��connect��close����Щ�����ˡ�Reactorģ�͵�ʵ���кܶ��֣������������������֣�
- ���̰߳�
- ���̰߳�
- ���Ӷ��̰߳�

###### Key Word��Java NIO��Reactorģ�ͣ�Java������̣�Event-Driven

### ���̰߳汾

�ṹͼ��������[Doug Lea](http://gee.cs.oswego.edu/)��Scalable IO in Java�����£�
![Reactorģ��ͼ-����](http://ob7uytere.bkt.clouddn.com/timestamp_1474469089432_test.png)

��ͼ��Reactor��һ�����͵��¼��������ģ��ͻ��˷������󲢽�������ʱ���ᴥ��ע���ڶ�·������Selector�ϵ�SelectionKey.OP_ACCEPT�¼������ڸ��¼��ϵ�Acceptor�����ְ����ǽ�������Ϊ�������Ķ�д������׼����

Reactor������£�
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
	 * �����׶�
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
	 * ��ѯ�׶�
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
	 * ���ڽ���TCP���ӵ�Acceptor
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
�����ǵ��͵ĵ��̰߳汾��Reactorʵ�֣�ʵ����Reactor����Ĺ����У��ڵ�ǰ��·������Selector��ע����OP_ACCEPT�¼�����OP_ACCEPT�¼�������Reactorͨ��dispatch����ִ��Acceptor��run������Acceptor�����Ҫ���ܾ��ǽ������󣬽������ӣ������������ӽ�����SocketChannel�Բ�������ʽ����Processor����

> Processor��������ǽ���I/O������

������Processor��Դ�룺
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
			 * write data to channel��
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
��ʵProcessorҪ��������ܼ򵥣�������selectorע�����Ȥ�Ķ�дʱ�䣬OP_READ��OP_WRITE��Ȼ��ȴ��¼�����������Ӧ�Ĳ�����
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
��doRead()��doSend()������΢������һ�㣬������ʵ��������TCPЭ�����ͨ��ʱ����Ҫ��������⣺**TCPճ���������**

###### TCPճ���������
���Ƕ�֪��TCPЭ���������ֽ����ģ����ֽ����������ģ��޷���Чʶ��Ӧ�ò����ݵı߽硣����ͼ��

![ճ�����ʾ��ͼ](http://ob7uytere.bkt.clouddn.com/timestamp_1479800845597_test.png)
��ͼ��ʾ��Ӧ�ò����������ݰ���D1��D2��D3.��Ӧ�ò����ݴ��������󣬿��ܻ����ճ���������

> TCPЭ��Ļ������䵥λ�Ǳ��ĶΣ���ÿ�����Ķ������Ч�غ��������Ƶ�,һ����̫��MTUΪ1500��ȥ��IPͷ20B��TCPͷ20B����ôʣ�µ�1460B���Ǵ��������Ķε���Ч�غɡ����Ӧ�ò����ݴ��ڸ�ֵ������ͼ�е����ݿ�D2������ô�����ͻ���в�����顣

**�������**
1. ��Ϣ������ͨ��˫�����͵���Ϣ�̶����ȣ�ȱ������ԣ��˷ѿɳܣ�������
2. ÿ����Ϣ֮��ӷָ����ȱ�㣺��Ϣ������ʱ�����������Ϣ���б�ʡ�Ͱ����ָ��ַ�����Ҫ����ת�壬Ч�ʵͣ�
3. ÿ�����ݰ��Ӹ�Header��������header��ָ���������ݵĳ��ȣ��ⲻ����Tcp��IpЭ��ͨ�õ�����ô������������

**���÷�����**

ʾ��ͼ���£�
![timestamp_1479519802200_test.png](http://ob7uytere.bkt.clouddn.com/timestamp_1479519802200_test.png)

header��ռ��4B������Ϊ���ݵĳ��ȡ�too simple������-_-

###### �������ˣ�������������Read��Write��ʵ�ֹ��̣�

**doRead**
inputBuffer����������ݣ�lenBuffer����������ݳ��ȣ���ʼ����ʱ�򣬽�lenBuffer����inputBuffer���������£�
```
private final ByteBuffer lenBuffer = ByteBuffer.allocate(4);
private ByteBuffer inputBuffer = lenBuffer;
```
1. ���inputBuffer == lenBuffer,��ô��inputBuffer�ж�ȡ��һ������ֵlen�����ֵ���ǽ�����Ҫ���ܵ����ݵĳ��ȡ�ͬʱ����һ����СΪlen���ڴ�ռ䣬�����Ƹ�inputBuffer��׼���������ݣ�����
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

2. ���inputBuffer ��= lenBuffer����ô��ʼ�������ݰɣ�
```
if(inputBuffer == lenBuffer){
        //������
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
**ע��**��
1. ���뱣֤�����������ģ���inputBuffer.hasRemaining()=false
2. processRequest�󣬽�inputBuffer���¸�ֵΪlenBuffer��Ϊ��һ�ζ�������׼����

**doWrite**

�û�����sendBuffer�����������ݣ���ʵ���ǽ����ݼ���outputQueue�����outputQueue����һ�����ͻ�����С�
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
doSend�����ͺܺ�����ˣ��޷Ǿ��ǲ��ϴ�outputQueue��ȡ���ݣ�Ȼ��д��channel�м��ɡ��������£�

�����Ͷ���outputQueue�е�����д�뻺����outputDirectBuffer��
1. ���outputDirectBuffer��Ϊ����������׼��
2. ��outputQueue����д��outputDirectBuffer
3. ����socketChannel.write(outputDirectBuffer);��outputDirectBufferд��socket������

> **ִ�в���2��ʱ�����ǿ��ܻ�������ô���������**
>> 1.ĳ�����ݰ���С������outputDirectBufferʣ��ռ��С
> 
>> 2.outputDirectBuffer�ѱ�����������outputQueue���д����͵�����

> **ִ�в���3��ʱ��Ҳ���ܳ����������������**
>> 1.outputDirectBuffer��ȫ��д��socket������
>
>> 2.outputDirectBufferֻ�в������ݻ���ѹ����û�����ݱ�д��socket������

ʵ�ֹ��̿��Խ��Դ�룬�����ص�������漸���㣺

> Ϊʲô��Ҫ����buf��position
```
int p = buf.position();
directBuffer.put(buf);
//reset position
buf.position(p);
```

д��directBuffer�������Ǽ�����д��SocketChannel�����ݣ���������ڣ������ǵ���
```
int sendSize = sc.write(directBuffer);
```
��ʱ��directBuffer�е����ݶ���д��Channel���������ǲ�ȷ���ģ�������Կ�java.nio.channels.SocketChannel.write(ByteBuffer src)��doc�ĵ���

> �����������ν��

˼·�ܼ򵥣�����write��������ֵsendSize������outputQueue�е�ByteBuffer������buf.remaining()��sendSize�Ĵ�С���ſ���ȷ��buf�Ƿ���ı������ˡ�������ʾ��
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
����ͨ�Ż������������Ĵ���˼·�ǲ���Zookeeper����ģ���ʵ�֣�����Ȥ���Կ�Zookeeper��ӦԴ�롣

#### �ܽ�
������ʵ�ַ�ʽ�У�dispatch������ͬ�������ģ��������е�IO������ҵ���߼�������NIO�̣߳���Reactor�̣߳�����ɡ����ҵ����ܿ죬��ô����ʵ�ַ�ʽûʲô���⣬�����л����û��̡߳����ǣ�����һ�����ҵ����ܺ�ʱ���漰�ܶ����ݿ���������̲����ȣ�����ô���������Reactor������������϶������ǲ�ϣ�������ġ���������ܼ򵥣�ҵ���߼������첽����,�������û��̴߳���

��������µ��̰߳��Reactorģ�͵�ȱ�㣺
- ��ʼ���ն�ֻ��һ��Reactor�̣߳�ȱ������ԣ�Reactor������ˣ�����ϵͳҲ���޷������������ɿ���̫�
- ���̵߳�����һ���������ڴ��ص�����£�Reactor�Ĵ����ٶȱ�Ȼ���Ϊϵͳ���ܵ�ƿ����


