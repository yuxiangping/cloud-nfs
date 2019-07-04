package org.yy.nfs.builder;

import java.math.BigDecimal;
import java.util.List;

import org.dom4j.Element;

/**
 * 节点builder工厂
 * @author yy
 */
public class NodeBuilder {

  private NodeBuilder() {}

  @SuppressWarnings("unchecked")
  public static Node bulider(Element element) {
    Node node = new Node();
    for (Element nd : (List<Element>) element.elements()) {
      String name = nd.getName();
      String value = nd.getTextTrim();

      if ("name".equals(name)) {
        node.setName(value);
      } else if ("host".equals(name)) {
        node.setHost(value);
      } else if ("port".equals(name)) {
        node.setPort(value);
      } else if ("path".equals(name)) {
        node.setPath(value);
      } else if ("size".equals(name)) {
        String unit = nd.attributeValue("unit");
        node.setSize(getSize(value, unit));
      } else {
        throw new RuntimeException("Error node name '" + name + "'.");
      }
    }
    return node;
  }

  private static double getSize(String size, String unit) {
    BigDecimal bd1 = new BigDecimal(Double.parseDouble(size));
    BigDecimal bd2 = new BigDecimal(Unit.fromName(unit).getRatio());
    return bd1.multiply(bd2).doubleValue();
  }

  /**
   * 空间大小 单位
   */
  private static enum Unit {
    MB(1), GB(1024), TB(1024 * 1024);

    /**
     * 比率
     */
    private double ratio;

    private Unit(double ratio) {
      this.ratio = ratio;
    }

    public static Unit fromName(String name) {
      if (name == null || name.length() == 0) {
        return MB;
      }
      for (Unit u : values()) {
        if (u.name().equals(name)) {
          return u;
        }
      }
      return MB;
    }

    public double getRatio() {
      return ratio;
    }
  }
}
