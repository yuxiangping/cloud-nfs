package com.cloud;

import java.util.List;

/**
 * @info :
 * @author: xiangping_yu
 * @data : 2013-12-2
 * @since : 1.5
 */
public class CloudManager {

  /**
   * 获取节点路径
   */
  public static Node getNode() {
    return CacheCloud.getInstance().getCloudNode();
  }

  /**
   * 刷新节点配置
   */
  public static boolean refresh() {
    return CacheCloud.getInstance().refresh();
  }

  /**
   * 预警节点信息
   */
  public static List<Node> getWarnNode() {
    return CacheCloud.getInstance().getWarnList();
  }

  /**
   * 统计节点硬盘大小
   */
  public double statisticalHD() {
    return CacheCloud.getInstance().statisticalHD();
  }

  /**
   * 统计已使用节点大小
   */
  public double usedHD() {
    return CacheCloud.getInstance().usedHD();
  }

}
