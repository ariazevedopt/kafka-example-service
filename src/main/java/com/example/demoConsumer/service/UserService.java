package com.example.demoConsumer.service;

import com.example.demoConsumer.database.UserModel;
import com.example.demoConsumer.database.UserRepository;
import com.kafka.schema.UserExample;
import com.kafka.schema.UserGroup;
import com.kafka.schema.UserList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void createMessageUser(UserExample data) {

        UserModel us = new UserModel();
        us.setName(data.getName());
        us.setAge(data.getAge());
        us.setGender(data.getGender());
        us.setGroupName(data.getGroup());

        userRepository.save(us);
    }

    public UserList getUsersByGroup(UserGroup data) {

        List<UserModel> userModel = userRepository.findByGroupName(data.getGroup());

        List<UserExample> list = new ArrayList<>();

        for (UserModel u : userModel) {
            list.add(new UserExample(u.getName(), u.getGender(), u.getAge(), u.getGroupName()));
        }
        return new UserList(list);
    }

}
