package com.daanpanis.injection.program;

public interface UserService {

    User getUser(int id);

    void addUser(int id, String name, int age);

}
