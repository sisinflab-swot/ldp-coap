package it.poliba.sisinflab.coap.ldp.handler;

import java.lang.management.ManagementFactory;
import java.util.Date;

import org.eclipse.rdf4j.model.vocabulary.DCTERMS;

import com.sun.management.OperatingSystemMXBean;

import it.poliba.sisinflab.coap.ldp.resources.LDPDataHandler;
import it.poliba.sisinflab.rdf.vocabulary.SSN_XG;

/**
 * Retrieves data about the system CPU load and update the LDP RDF repository 
 * <p>
 * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/lang/management/OperatingSystemMXBean.html">OperatingSystemMXBean</a>
 *
 */

@SuppressWarnings("restriction")
public class CPUHandler extends LDPDataHandler {
	
	OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
	
	@Override
	protected void handleData() {
		double load = bean.getSystemCpuLoad();
		if (mng != null && mng.connected()) {
			mng.updateRDFLiteralStatement(mng.getBaseURI() + resource, SSN_XG.hasValue.stringValue(), load);
			mng.updateRDFLiteralStatement(mng.getBaseURI() + resource, DCTERMS.CREATED.toString(), new Date());
		}
	}

}
