package com.bigdata.platform.scheduler.web.server.vo;

import java.io.Serializable;
import java.util.List;

import org.apache.curator.shaded.com.google.common.collect.Lists;

import com.bigdata.platform.scheduler.dal.enums.DependTypeEnum;

import lombok.Data;

@Data
public class Vertex extends VertexBase {

	private static final long serialVersionUID = -3262947654398920750L;

	private List<Edge> childs = Lists.newArrayList();
	
	private List<Edge> parents = Lists.newArrayList();
	
	public void addChild(Edge edge) {
		this.childs.add(edge);
	}
	
	public void addParent(Edge edge) {
		this.parents.add(edge);
	}
	
	/**
	 * 边
	 *
	 * @author zhoulijiang
	 * @date 2019-12-26 18:11
	 *
	 */
	@Data
	public static class Edge  implements Serializable {
		
		private static final long serialVersionUID = 1712242385323216565L;

		private Vertex vertex;
		
		private DependTypeEnum type;
		
	}

}
