/**
 * Copyright 2016 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cc.kave.commons.model.groum;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

public class Groum implements IGroum {
	DirectedGraph<Node, LabelledEdge> groum;
	Node root;
	private String methodSignature;

	public Groum(String methodSignature, Set<Node> nodes, Set<Pair<Node, Node>> edges) {
		this.methodSignature = methodSignature;
		groum = new DefaultDirectedGraph<Node, LabelledEdge>(LabelledEdge.class);
		nodes.forEach(node -> groum.addVertex(node));
		edges.forEach(edge -> groum.addEdge(edge.getLeft(), edge.getRight()));
	}

	public String getMethodSignature() {
		return methodSignature;
	}

	public boolean containsNode(Node node) {
		return groum.containsVertex(node);
	}

	@Override
	public int getNodeCount() {
		return groum.vertexSet().size();
	}

	public Set<Node> getAllNodes() {
		return groum.vertexSet();
	}

	public int getEdgeCount() {
		return groum.edgeSet().size();
	}

	@Override
	public Set<Node> getPredecessors(Node node) {
		Set<Node> predecessors = new HashSet<>();
		Set<LabelledEdge> incomingEdges = groum.incomingEdgesOf(node);

		for (LabelledEdge edge : incomingEdges) {
			predecessors.add(groum.getEdgeSource(edge));
		}
		return predecessors;
	}

	@Override
	public Set<Node> getSuccessors(Node node) {
		Set<Node> successors = new HashSet<>();
		Set<LabelledEdge> outgoingEdges = groum.outgoingEdgesOf(node);

		for (LabelledEdge edge : outgoingEdges) {
			successors.add(groum.getEdgeTarget(edge));
		}
		return successors;
	}

	@Override
	public String toString() {
		return groum.toString();
	}

	@Override
	public Node getRoot() {
		if (root == null) {
			root = findRoot();
		}
		return root;
	}

	private Node findRoot() {
		Node root = null;
		for (Node node : getAllNodes()) {
			if (groum.inDegreeOf(node) == 0) {
				if (root != null) {
					throw new IllegalStateException("groum has multiple roots: " + toString());
				}
				root = node;
			}
		}
		if (root == null) {
			throw new IllegalStateException("groum has no root: " + toString());
		}
		return root;
	}
}
