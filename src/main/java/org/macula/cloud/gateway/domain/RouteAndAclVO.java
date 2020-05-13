package org.macula.cloud.gateway.domain;

import java.io.Serializable;
import java.util.List;

/**
 * Created by linqina on 2018/9/20 下午6:33.
 */
public class RouteAndAclVO implements Serializable {

    private static final long serialVersionUID = -7044583428774687535L;

    private String id;
    private String uri;
    private String path;
    private Integer stripPrefix;
    private Integer order;
    private List<String> method;
    private String group;
    private String weight;
    private String needSignIn;
    private List<String> roleIds;
    private List<String> userIds;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getStripPrefix() {
        return stripPrefix;
    }

    public void setStripPrefix(Integer stripPrefix) {
        this.stripPrefix = stripPrefix;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public List<String> getMethod() {
        return method;
    }

    public void setMethod(List<String> method) {
        this.method = method;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getNeedSignIn() {
        return needSignIn;
    }

    public void setNeedSignIn(String needSignIn) {
        this.needSignIn = needSignIn;
    }

    public List<String> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<String> roleIds) {
        this.roleIds = roleIds;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
}
