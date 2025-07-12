package com.phoneapp.phonepulse.data.network;

import com.phoneapp.phonepulse.models.User;

import java.util.List;

public class UserListResponse {
    private List<User> users;
    private int total;
    private int page;
    private int limit;

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
