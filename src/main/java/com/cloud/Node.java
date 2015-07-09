package com.cloud;

import com.utils.closure.Convertable;
import com.utils.closure.Statisticable;

/**
 * @info : 集群节点
 * @author: xiangping_yu
 * @data : 2013-12-2
 * @since : 1.5
 */
public class Node implements Cloneable {

  /**
   * 节点预警大小 （单位：M）
   */
  private static final double WARN_FIXED_SIZE = 500;

  private static final double WARN_SIZE_PERCENT = 0.1;

  /**
   * 节点名
   */
  private String name;
  /**
   * 主机名/ip
   */
  private String host;
  /**
   * 端口
   */
  private String port;
  /**
   * 节点路径
   */
  private String path;
  /**
   * 节点分配大小
   */
  private double size;
  /**
   * 已使用
   */
  private double used;

  public static final Convertable<Node, String> TO_HOST = new Convertable<Node, String>() {
    public String convert(Node q) {
      return q.getHost();
    }
  };

  public static final Statisticable<Double, Node> COUNT_SIZE = new Statisticable<Double, Node>() {
    public Double statistic(Double v1, Node v2) {
      return v1 + v2.getSize();
    }
  };

  public static final Statisticable<Double, Node> COUNT_USED = new Statisticable<Double, Node>() {
    public Double statistic(Double v1, Node v2) {
      return v1 + v2.getUsed();
    }
  };

  /**
   * 是否到达预警容量
   */
  public boolean isWarn() {
    double WARN_SIZE = size * WARN_SIZE_PERCENT;
    if (WARN_SIZE >= WARN_FIXED_SIZE) {
      return (size - used) <= WARN_FIXED_SIZE;
    } else {
      return (size - used) <= WARN_SIZE;
    }
  }

  /**
   * 节点可用大小
   */
  public double getAvailable() {
    return (size - used);
  }

  /**
   * 概率规则
   */
  public static class ConfigRate {
    private Node node;
    private long rate;

    public ConfigRate(Node node, long rate) {
      this.node = node;
      this.rate = rate;
    }

    /**
     * 是否命中
     */
    public boolean isHit(int _rate) {
      return _rate < rate;
    }

    public Node getNode() {
      return node;
    }

    public long getRate() {
      return rate;
    }
  }

  @Override
  protected Node clone() throws CloneNotSupportedException {
    Node node = new Node();
    node.setName(name);
    node.setHost(host);
    node.setPath(port);
    node.setPath(path);
    node.setSize(size);
    node.setUsed(used);
    return node;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public double getSize() {
    return size;
  }

  public void setSize(double size) {
    this.size = size;
  }

  public double getUsed() {
    return used;
  }

  public void setUsed(double used) {
    this.used = used;
  }
}
