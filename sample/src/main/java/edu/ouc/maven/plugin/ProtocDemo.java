package edu.ouc.maven.plugin;

import com.example.tutorial.PersonProtos;

public class ProtocDemo {
    public static void main(String[] args) {
        PersonProtos.Person person = PersonProtos.Person.newBuilder()
                .setId(0)
                .setName("wqx")
                .setEmail("topgunviper@163.com")
                .build();
        System.out.println(person);
    }
}
