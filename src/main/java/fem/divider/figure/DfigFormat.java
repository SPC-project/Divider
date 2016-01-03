/*
 * DfigFormat.java
 *
 * Created on Sunday, 27, March 2005, 8:51
 */

package fem.divider.figure;

import fem.divider.*;
import fem.geometry.DotMaterial;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;

/**
 * This class is responsible for loading/saving figures in .dfig format
 * 
 * @author gefox
 * @version
 */
public class DfigFormat extends AbstractFormat {

	/** Creates new DfigFormat */
	public DfigFormat() {
		factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println("Parser configuration exception");
		}
		try {
			trans = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e) {
			System.err.println("Transformer configuration exception");
		}
	}

	/*
	 * OPEN THE FIGURE
	 */
	Figure open(World world_, InputStream in) {
		Figure fig = new Figure();
		try {
			Document doc = builder.parse(in);
			Element figureElement = doc.getDocumentElement();
			double left = Double
					.parseDouble(figureElement.getAttribute("left")); //$NON-NLS-1$
			double right = Double.parseDouble(figureElement
					.getAttribute("right")); //$NON-NLS-1$
			double bottom = Double.parseDouble(figureElement
					.getAttribute("bottom")); //$NON-NLS-1$
			double top = Double.parseDouble(figureElement.getAttribute("top")); //$NON-NLS-1$
			world_.resize(left, right, bottom, top);

			NodeList contours = figureElement.getChildNodes();
			// look for a contours
			for (int ci = 0; ci < contours.getLength(); ci++) {
				org.w3c.dom.Node cn = contours.item(ci);
				if (cn.getNodeName().equals("Contour")) //$NON-NLS-1$
				{// we've got a contour
					org.w3c.dom.Element ce = (org.w3c.dom.Element) cn;
					Contour contour = new Contour(fig);
					contour.setPositive(Boolean.valueOf(
							ce.getAttribute("positive")).booleanValue()); //$NON-NLS-1$
					NodeList nodes = ce.getChildNodes();
					ArrayList czoneList = new ArrayList(5); // czone
					// list
					// ---
					// we
					// will
					// fill
					// it
					// and
					// then
					// attach
					// to
					// contour
					// by
					// elements
					ArrayList nodeCZonesList;
					// look for a nodes
					for (int ni = 0; ni < nodes.getLength(); ni++) {
						org.w3c.dom.Node nn = nodes.item(ni);
						if (nn.getNodeName().equals("Node")) //$NON-NLS-1$
						{// we've got a node
							org.w3c.dom.Element ne = (org.w3c.dom.Element) nn;
							double x = Double.parseDouble(ne.getAttribute("x")); //$NON-NLS-1$
							double y = Double.parseDouble(ne.getAttribute("y")); //$NON-NLS-1$
							DotMaterial m = DotMaterial.valueOf(ne.getAttribute("material"));
							Node node = new Node(x, y, m);
							contour.rawAppend(node);
							// put end of previous segment (if exists) to new
							// node
							if (contour.nodes.size() > 1) {
								Node lastNode = (Node) contour.nodes
										.get(contour.nodes.size() - 2);
								Segment prevSegment = lastNode
										.getNextSegment();
								prevSegment.setNewEnd(node);
							}
							Node nextNode = (Node) contour.nodes.get(0);

							Segment segment = new Segment(node,
									nextNode);

							// <Node> get what is
							// here </Node>
							NodeList nodeSubs = ne.getChildNodes();

							// for this node create currently empty list of
							// czones
							nodeCZonesList = new ArrayList(0);
							czoneList.add(nodeCZonesList);

							// search for Node subnodes
							for (int nsi = 0; nsi < nodeSubs.getLength(); nsi++) {
								org.w3c.dom.Node nsnode = nodeSubs.item(nsi);
								// If geometry found
								if (nsnode.getNodeName().equals(
										DOM_ELEMENT_GEOMETRY)) {
									org.w3c.dom.Element nse = (org.w3c.dom.Element) nsnode;
									AbstractSegmentGeometry geometry = createSegmentGeometry(nse);
									segment.setGeometry(geometry);
								}
								// CZone found
								if (nsnode.getNodeName().equals(
										DOM_ELEMENT_CZONE)) //$NON-NLS-1$
								{
									org.w3c.dom.Element nse = (org.w3c.dom.Element) nsnode;

									CZone czone = new CZone();
									czone.setName(nse.getAttribute("Name")); //$NON-NLS-1$
									czone.setGroupName(nse
											.getAttribute("GroupName")); //$NON-NLS-1$
									czone.setOffsetMode(Integer.parseInt(nse
											.getAttribute("offsetMode"))); //$NON-NLS-1$
									czone.setOffsetVal(Double.parseDouble(nse
											.getAttribute("offsetVal"))); //$NON-NLS-1$
									czone.setLengthMode(Integer.parseInt(nse
											.getAttribute("lengthMode"))); //$NON-NLS-1$
									czone.setLengthVal(Double.parseDouble(nse
											.getAttribute("lengthVal"))); //$NON-NLS-1$
									// influence-related
									// properties
									czone.setInfluenceMode(Integer.parseInt(nse
											.getAttribute("influenceMode"))); //$NON-NLS-1$
									czone
											.setForbidXMotion(Boolean
													.valueOf(
															nse
																	.getAttribute("fixX")).booleanValue()); //$NON-NLS-1$
									czone
											.setForbidYMotion(Boolean
													.valueOf(
															nse
																	.getAttribute("fixY")).booleanValue()); //$NON-NLS-1$
									czone
											.setForceZeroDirection(Integer
													.parseInt(nse
															.getAttribute("forceZeroDirection"))); //$NON-NLS-1$
									czone
											.setForceDirectionDeg(Double
													.parseDouble(nse
															.getAttribute("forceDirection"))); //$NON-NLS-1$
									czone.setForceValue(Double.parseDouble(nse
											.getAttribute("forceValue"))); //$NON-NLS-1$

									// Add new czone to array
									nodeCZonesList.add(czone);
								}
							}
							// end search for Node subnodes
						}
					}// end of look for nodes
					//
					// WALK AGAIN: create default segments
					// between nodes in this contour
					//
					if (contour.nodes.size() == 0)
						continue; // no
					// add contour to figure
					fig.addContour(contour);
					// TODO: delete next line
					contour.nodes.add(contour.nodes.get(0)); // TEMPORARY
					// ADD node to end of contour
					// (done to enclose segments)
					Iterator<Node> i = contour.nodes.iterator();
					Segment segment;
					Node n1;
					Node n2;
					n2 = (Node) i.next();
					int ni = 0;
					// for rest of nodes in contour
					while (i.hasNext()) {
						n1 = n2;
						n2 = (Node) i.next();
						// segment = new DefaultSegment(n1, n2);
						segment = n1.getNextSegment();
						nodeCZonesList = (ArrayList) czoneList.get(ni);
						if (nodeCZonesList.size() > 0) // We've
						// got some contact zones!
						{
							segment.setCZones(nodeCZonesList);
						}

						ni++;
					}
					// TODO: delete next line
					contour.nodes.remove(contour.nodes.size() - 1); // remove
					// TEMPORARY
					// node
					// from
					// end
					// of
					// contour

				}// end of
				// if(cn.getNodeName().equals("Contour"))
				if (cn.getNodeName().equals("MeshSettings"))// Mesh
				// settings
				// //$NON-NLS-1$
				{
					org.w3c.dom.Element ce = (org.w3c.dom.Element) cn;

					String attr;

					attr = ce.getAttribute("maxArea"); //$NON-NLS-1$
					if (attr != null && attr.length() != 0) {
						double maxArea = Double.parseDouble(attr);
						fig.getMeshSettings().setMaxArea(maxArea);
					}

					attr = ce.getAttribute("minArea"); //$NON-NLS-1$
					if (attr != null && attr.length() != 0) {
						double minArea = Double.parseDouble(attr);
						fig.getMeshSettings().setMinArea(minArea);
					}

					attr = ce.getAttribute("minAngle"); //$NON-NLS-1$
					if (attr != null && attr.length() != 0) {
						double minAngle = Double.parseDouble(attr);
						fig.getMeshSettings().setMinAngle(minAngle);
					}

				}// end of
				// if(cn.getNodeName().equals("MeshSettings"))
			}// end of look for contours
		} catch (SAXException e) {
			System.err.println("SAX Exception");
			return null;
		} catch (IOException e) {
			System.err.println("IO Exception");
			return null;
		}

		return fig;
	}

	/*
	 * SAVE THE FIGURE
	 */
	String save(Figure fig, OutputStream out) {
		Document doc = builder.newDocument();
		Element figureElement = doc.createElement("Figure"); //$NON-NLS-1$
		doc.appendChild(figureElement);// create root node (Figure)
		figureElement.setAttribute("left", "" + fig.panel.getWorld().getLeft()); //$NON-NLS-1$ //$NON-NLS-2$
		figureElement.setAttribute(
				"right", "" + fig.panel.getWorld().getRight()); //$NON-NLS-1$ //$NON-NLS-2$
		figureElement.setAttribute(
				"bottom", "" + fig.panel.getWorld().getBottom()); //$NON-NLS-1$ //$NON-NLS-2$
		figureElement.setAttribute("top", "" + fig.panel.getWorld().getTop()); //$NON-NLS-1$ //$NON-NLS-2$

		Element contourElement;
		Element nodeElement;
		Element segmentElement;
		Element czoneElement;

		// append contours to figure
		for( Contour contour : fig.getContours() ) {
			contourElement = doc.createElement("Contour"); //$NON-NLS-1$
			contourElement.setAttribute("positive", "" + contour.isPositive()); //$NON-NLS-1$ //$NON-NLS-2$

			figureElement.appendChild(contourElement);

			// append nodes to contour
			for( Node node : contour.nodes ) {
				nodeElement = doc.createElement("Node"); //$NON-NLS-1$
				nodeElement.setAttribute("x", Double.toString(node.getX())); //$NON-NLS-1$
				nodeElement.setAttribute("y", Double.toString(node.getY())); //$NON-NLS-1$
				nodeElement.setAttribute("material", node.getMaterial().name());

				Segment segment = node.getNextSegment();
				// append czones
				if (segment.czones != null) {
					for (Iterator czi = segment.czones.iterator(); czi
							.hasNext();) {
						CZone czone = (CZone) czi.next();
						czoneElement = doc.createElement("CZone"); //$NON-NLS-1$
						czoneElement.setAttribute("Name", czone.getName()); //$NON-NLS-1$
						czoneElement.setAttribute(
								"GroupName", czone.getGroupName()); //$NON-NLS-1$
						czoneElement.setAttribute(
								"offsetMode", "" + czone.getOffsetMode()); //$NON-NLS-1$ //$NON-NLS-2$
						czoneElement.setAttribute(
								"offsetVal", "" + czone.getOffsetVal()); //$NON-NLS-1$ //$NON-NLS-2$
						czoneElement.setAttribute(
								"lengthMode", "" + czone.getLengthMode()); //$NON-NLS-1$ //$NON-NLS-2$
						czoneElement.setAttribute(
								"lengthVal", "" + czone.getLengthVal()); //$NON-NLS-1$ //$NON-NLS-2$
						// influence-specifi
						czoneElement.setAttribute(
								"influenceMode", "" + czone.getInfluenceMode()); //$NON-NLS-1$ //$NON-NLS-2$
						czoneElement.setAttribute(
								"fixX", "" + czone.isForbidXMotion()); //$NON-NLS-1$ //$NON-NLS-2$
						czoneElement.setAttribute(
								"fixY", "" + czone.isForbidYMotion()); //$NON-NLS-1$ //$NON-NLS-2$
						czoneElement
								.setAttribute(
										"forceZeroDirection", "" + czone.getForceZeroDirection()); //$NON-NLS-1$ //$NON-NLS-2$
						czoneElement
								.setAttribute(
										"forceDirection", "" + czone.getForceDirectionDeg()); //$NON-NLS-1$ //$NON-NLS-2$
						czoneElement.setAttribute(
								"forceValue", "" + czone.getForceValue()); //$NON-NLS-1$ //$NON-NLS-2$

						nodeElement.appendChild(czoneElement);
					}
				}// end of append czones
				// save geometry of segment
				Element geometryElement = createSegmentGeometryElement(doc,
						segment.getGeometry());
				if (geometryElement != null)
					nodeElement.appendChild(geometryElement);
				// end save geometry of segment
				contourElement.appendChild(nodeElement);
				// Here can be code to append segment
			}// end of append nodes
		}// end of append contours

		Element meshSettingsElement = doc.createElement("MeshSettings"); //$NON-NLS-1$
		figureElement.appendChild(meshSettingsElement);
		meshSettingsElement.setAttribute("maxArea", //$NON-NLS-1$
				Double.toString(fig.getMeshSettings().getMaxArea()));
		meshSettingsElement.setAttribute("minAngle", //$NON-NLS-1$
				Double.toString(fig.getMeshSettings().getMinAngle()));
		meshSettingsElement.setAttribute("minArea", //$NON-NLS-1$
				Double.toString(fig.getMeshSettings().getMinArea()));

		try {
			trans.transform(new javax.xml.transform.dom.DOMSource(doc),
					new javax.xml.transform.stream.StreamResult(out));
		} catch (TransformerException e) {
			String msg = "Transformer exception";
			System.err.println(msg);
			return msg;
		}
		return null;
	}

	/**
	 * Generate DOM element for segment geometry. Also sets all attributes of
	 * this element
	 * 
	 * @param doc
	 * @param geometry
	 * @return
	 */
	private static Element createSegmentGeometryElement(Document doc,
			AbstractSegmentGeometry geometry) {
		Element geometryElement = doc.createElement(DOM_ELEMENT_GEOMETRY);
		Map<String, String> params = geometry.getParameters();
		Set<Map.Entry<String, String>> entrySet = params.entrySet();

		Iterator<Map.Entry<String, String>> it = entrySet.iterator();
		Map.Entry<String, String> entry;
		while (it.hasNext()) {
			entry = it.next();
			geometryElement.setAttribute(entry.getKey(), entry.getValue());
		}

		return geometryElement;
	}// end createSegmentGeometryElement

	/**
	 * Creates and returns segment geometry from information, specified in DOM
	 * element. If there's no attributes in element, returns null;
	 * 
	 * @param element
	 * @return
	 */
	private static AbstractSegmentGeometry createSegmentGeometry(
			org.w3c.dom.Element element) {
		NamedNodeMap nnmap = element.getAttributes();
		if (nnmap == null)
			return null;

		Map<String, String> params = new HashMap<String, String>();
		int len = nnmap.getLength();
		int i;
		for (i = 0; i < len; i++) {
			org.w3c.dom.Node node = nnmap.item(i);
			params.put(node.getNodeName(), node.getNodeValue());
		}
		AbstractSegmentGeometry geometry;
		if (ArcSegmentGeometry.GEOMETRY_TYPE.equals(params.get(ArcSegmentGeometry.PARAM_GEOMETRY_TYPE))) {
			geometry = new ArcSegmentGeometry();
			geometry.setParameters(params);
		} else {
			geometry = new LineSegmentGeometry();
		}

		return geometry;
	}

	private static final String DOM_ELEMENT_NODE = "Node";
	private static final String DOM_ELEMENT_GEOMETRY = "Geometry";
	private static final String DOM_ELEMENT_CZONE = "CZone";

	DocumentBuilderFactory factory;
	DocumentBuilder builder;
	Transformer trans;
	// static {
	// FigureStreamer.setDefaultFormat(new DfigFormat());
	// }
}
