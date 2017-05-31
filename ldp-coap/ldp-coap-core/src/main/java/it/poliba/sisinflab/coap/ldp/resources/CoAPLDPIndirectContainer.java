package it.poliba.sisinflab.coap.ldp.resources;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;

import it.poliba.sisinflab.coap.ldp.LDP;
import it.poliba.sisinflab.coap.ldp.exception.CoAPLDPContentFormatException;
import it.poliba.sisinflab.coap.ldp.exception.CoAPLDPException;

/**
 * Represents an LDP Indirect Container
 * <p> 
 * @see <a href="https://www.w3.org/TR/ldp/#ldpic">#LDP Indirect Container</a>
 *
 */

public class CoAPLDPIndirectContainer extends CoAPLDPContainer {

	CoAPLDPRDFSource resource;
	String memberRelation;
	String insertedContentRelation;
	int anonymous = 0;
	
	/**
	 * Creates a new LDP Indirect Container.
	 *
	 * @param  	name 						the name of the contained resource
	 * @param	mng							the reference resource manager
	 * @param  	member 						the name of the member resource of the created Indirect Container
	 * @param  	memberType 					the type of the member resource of the created Indirect Container
	 * @param  	memberRelation 				the memberRelation property of the created Indirect Container
	 * @param  	insertedContentRelation 	the insertedContentRelation property of the created Indirect Container
	 * 
	 * @see CoAPLDPResourceManager
	 */
	public CoAPLDPIndirectContainer(String name, CoAPLDPResourceManager mng, CoAPLDPRDFSource resource,
			String memberRelation, String insertedContentRelation) {
		super(name, "", mng);
		this.memberRelation = memberRelation;
		this.insertedContentRelation = insertedContentRelation;

		this.name = "/" + name;

		init();
		addMembershipResource(resource, ""); // add inserted-content-relation
	}

	/**
	 * Creates a new LDP Indirect Container (as subresource).
	 *
	 * @param  	name 						the name of the contained resource
	 * @param	path						the path of the root resource
	 * @param	mng							the reference resource manager
	 * @param  	member 						the name of the member resource of the created Indirect Container
	 * @param  	memberType 					the type of the member resource of the created Indirect Container
	 * @param  	memberRelation 				the memberRelation property of the created Indirect Container
	 * @param  	insertedContentRelation 	the insertedContentRelation property of the created Indirect Container
	 * 
	 * @see CoAPLDPResourceManager
	 */
	public CoAPLDPIndirectContainer(String name, String path, CoAPLDPResourceManager mng, CoAPLDPRDFSource resource,
			String memberRelation, String insertedContentRelation) {
		super(name, path, mng);
		this.memberRelation = memberRelation;
		this.insertedContentRelation = insertedContentRelation;

		this.name = path + "/" + name;

		init();
		addMembershipResource(resource, path); // add inserted-content-relation
	}

	private void init() {
		this.fRDFType = LDP.CLASS_INDIRECT_CONTAINER;

		getAttributes().addResourceType(LDP.CLASS_INDIRECT_CONTAINER);

		mng.addRDFIndirectContainer(mng.getBaseURI() + name);
	}

	/**
	 * Returns the member resource.
	 *
	 * @return CoAPLDPRDFSource	the container member resource
	 * 
	 */
	public CoAPLDPRDFSource getMemberResource() {
		return this.resource;
	}

	private void addMembershipResource(CoAPLDPRDFSource resource, String path) {
		this.resource = resource;
		add(resource);
		mng.setLDPMembershipResource(mng.getBaseURI() + path + "/" + resource.getURI(), mng.getBaseURI() + name);
		mng.setLDPMemberRelation(mng.getBaseURI() + name, this.memberRelation);
		mng.setLDPInsertedContentRelation(mng.getBaseURI() + name, this.insertedContentRelation);
	}

	/**
	 * Manages LDP-CoAP DELETE requests.
	 *
	 * @param  exchange 	the request object
	 * 
	 * @see CoapExchange
	 * 
	 */
	public void handleDELETE(CoapExchange exchange) {
		mng.deleteRDFDirectContainer(mng.getBaseURI() + this.getURI());
		// mng.deleteRDFSource(mng.getBaseURI() + this.getURI());
		this.delete();
		exchange.respond(ResponseCode.DELETED);
	}

	private boolean existChild(String childName) {
		for (Resource r : resource.getChildren()) {
			if (r.getURI().equals(childName))
				return true;
		}
		return false;
	}

	private String checkURI(String s) {
		if (s.startsWith("/"))
			return s.substring(1);
		else
			return s;
	}

	private void addRDFResource(CoAPLDPResource res, IRI contentRes) {
		resource.add(res);

		String r = checkURI(res.getFullName());
		String m = checkURI(resource.getFullName());
		String c = checkURI(name);

		mng.setLDPContainsRelationship(mng.getBaseURI() + "/" + r, mng.getBaseURI() + "/" + c);
		mng.setRDFStatement(mng.getBaseURI() + "/" + m, this.memberRelation, contentRes.toString());
	}

	private void addRDFResource(CoAPLDPResource res) {
		resource.add(res);

		String r = checkURI(res.getFullName());
		String c = checkURI(name);

		mng.setLDPContainsRelationship(mng.getBaseURI() + "/" + r, mng.getBaseURI() + "/" + c);
	}

	/**
	 * Manages LDP-CoAP POST requests.
	 *
	 * @param  exchange 	the request object
	 * 
	 * @see CoapExchange
	 * 
	 */
	@Override
	public void handlePOST(CoapExchange exchange) {
		this.postResource(exchange, false);
	}
	
	@Override
	protected void handleLDPPutToCreate(CoapExchange exchange) {
		this.postResource(exchange, true);
	}
	
	private void postResource(CoapExchange exchange, boolean putToCreate){
		Request req = exchange.advanced().getCurrentRequest();
		HashMap<String, String> atts = serializeAttributes(req.getOptions().getUriQuery());

		String title = atts.get(LinkFormat.TITLE);
		if (title == null) {
			title = getAnonymousResource();
		}

		int ct = exchange.getRequestOptions().getContentFormat();

		if ((ct != -1) && (title != null)) {

			String body = exchange.getRequestText();
			try {
				String childName = resource.getURI() + "/" + title;

				if (mng.isDeleted(childName)) {
					if(!putToCreate){
						title = getAnonymousResource();
						childName = resource.getURI() + "/" + title;
					} else {
						throw new CoAPLDPException("LDP Resource previously deleted!");
					}
				}

				if (!existChild(childName)) {

					if (ct == MediaTypeRegistry.TEXT_TURTLE || ct == MediaTypeRegistry.APPLICATION_LD_JSON) {

						RDFFormat f;
						if (ct == MediaTypeRegistry.TEXT_TURTLE)
							f = RDFFormat.TURTLE;
						else
							f = RDFFormat.JSONLD;

						String indirectResource = mng.postIndirectRDFSource(mng.getBaseURI() + childName, body,
								this.insertedContentRelation, f);

						String rt = atts.get(LinkFormat.RESOURCE_TYPE);
						if ((rt == null) || (rt.equals(LDP.LINK_LDP + ":" + LDP.CLASS_LNAME_RESOURCE))) {
							/*** Add LDP-RDFSource ***/
							CoAPLDPRDFSource s = new CoAPLDPRDFSource(title, resource.getURI(), mng);
							if (indirectResource != null)
								this.addRDFResource(s, mng.createIRI(indirectResource.replaceAll("<>", "")));
							else
								this.addRDFResource(s);
						} else if (rt.equals(LDP.LINK_LDP + ":" + LDP.CLASS_LNAME_BASIC_CONTAINER)) {
							/*** Add LDP-BasicContainer ***/
							CoAPLDPBasicContainer bc = new CoAPLDPBasicContainer(title, mng);
							bc.setRDFCreated();
							this.addRDFResource(bc, mng.createIRI(indirectResource));
						} else
							throw new CoAPLDPException("Invalid RT query parameter.");
					} else {
						throw new CoAPLDPContentFormatException("Content-Format (CT) Not Accepted.");
					}

					exchange.setLocationPath(mng.getBaseURI() + childName);
					exchange.setLocationQuery(
							LinkFormat.RESOURCE_TYPE + "=" + LDP.LINK_LDP + ":" + LDP.CLASS_LNAME_RESOURCE);
					exchange.respond(ResponseCode.CREATED);
				} else
					exchange.respond(ResponseCode.FORBIDDEN);

			} catch (CoAPLDPContentFormatException e) {
				e.printStackTrace();
				exchange.respond(ResponseCode.UNSUPPORTED_CONTENT_FORMAT);
			} catch (RDFParseException | CoAPLDPException e) {
				e.printStackTrace();
				exchange.respond(ResponseCode.BAD_REQUEST);
			} catch (RepositoryException | IOException e) {
				e.printStackTrace();
				exchange.respond(ResponseCode.INTERNAL_SERVER_ERROR);
			}
		} else {
			exchange.respond(ResponseCode.BAD_REQUEST);
		}
	}

	public CoAPLDPRDFSource createRDFSource(String name) {
		CoAPLDPRDFSource res = new CoAPLDPRDFSource(name, resource.getFullName(), mng);
		this.addRDFResource(res);
		return res;
	}

	public CoAPLDPRDFSource createRDFSource(String name, String type) {
		CoAPLDPRDFSource res = new CoAPLDPRDFSource(name, resource.getFullName(), mng, type);
		this.addRDFResource(res);
		return res;
	}
	
	/**
	 * Creates a new LDP RDF Source as contained object.
	 *
	 * @param  	name 	the name of the contained resource
	 * @param	uri		the member-derived-URI of the contained resource
	 * 
	 * @return CoAPLDPRDFSource		the created resource
	 */
	public CoAPLDPRDFSource createRDFSourceWithDerivedURI(String name, String uri) {
		CoAPLDPRDFSource res = new CoAPLDPRDFSource(name, resource.getFullName(), mng);
		this.addRDFResource(res, mng.createIRI(uri));
		return res;
	}
	
	/**
	 * Creates a new LDP RDF Source as contained object.
	 *
	 * @param  	name 	the name of the contained resource
	 * @param	type	the type of the contained resource
	 * @param	uri		the member-derived-URI of the contained resource
	 * 
	 * @return CoAPLDPRDFSource		the created resource
	 */
	public CoAPLDPRDFSource createRDFSourceWithDerivedURI(String name, String type, String uri) {
		CoAPLDPRDFSource res = new CoAPLDPRDFSource(name, resource.getFullName(), mng, type);
		this.addRDFResource(res, mng.createIRI(uri));
		return res;
	}


	public CoAPLDPBasicContainer createBasicContainer(String name) {
		CoAPLDPBasicContainer bc = new CoAPLDPBasicContainer(name, resource.getFullName(), mng);
		this.addRDFResource(bc);
		return bc;
	}

	public CoAPLDPNonRDFSource createNonRDFSource(String name, int mediaType) {
		CoAPLDPNonRDFSource nr = new CoAPLDPNonRDFSource(name, getFullName(), mng, mediaType);
		this.addRDFResource(nr);
		return nr;
	}

	public CoAPLDPIndirectContainer createIndirectContainer(String name, String member, String memberType,
			String memberRelation, String insertedContentRelation) {
		CoAPLDPRDFSource memberResIC = new CoAPLDPRDFSource(member, resource.getFullName()+"/"+name, mng, memberType);       
        CoAPLDPIndirectContainer ic = new CoAPLDPIndirectContainer(name, resource.getFullName(), mng, memberResIC, memberRelation, insertedContentRelation);   
        this.addRDFResource(ic);
		return ic;
	}

	public CoAPLDPDirectContainer createDirectContainer(String name, String member, String memberType,
			String memberRelation, String isMemberOfRelation) {
		CoAPLDPRDFSource memberRes = new CoAPLDPRDFSource(member, resource.getFullName() + "/" + name, mng, memberType);
		CoAPLDPDirectContainer dc = null;
		try {
			dc = new CoAPLDPDirectContainer(name, resource.getFullName(), mng, memberRes, memberRelation,
					isMemberOfRelation);
			this.addRDFResource(dc);
		} catch (CoAPLDPException e) {
			e.printStackTrace();
		}
		return dc;
	}

}
