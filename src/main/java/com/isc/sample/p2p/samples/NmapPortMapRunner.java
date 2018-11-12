/**
 *
 */
package com.isc.sample.p2p.samples;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ihsanCaliskan
 */
public class NmapPortMapRunner extends ProcessMaster {

	private static final Logger			LOGGER				= LoggerFactory.getLogger(NmapPortMapRunner.class);

	/* opened ports from top 10 tcp ports
	* nmap -sU -p 5351 10.6.0.254 --script nat-pmp-mapport --script-args='op=map,pubport=30540,privport=1235,protocol=udp'
	* */
	public static String portMap = "-sU -p 5351 #ip --script nat-pmp-mapport --script-args='op=map,pubport=#pubPort,privport=#prvPort,protocol=udp'";


	public NmapPortMapRunner() {
	}

	@Override
	protected String getDefaultCommand() {
		return "nmap";
	}

	@Override
	public void refresh() {

		super.refresh();
	}

	public void portMap(String ip, int pubPort , int prvPort) {
		String prm [] = portMap.replace("#pubPort", String.valueOf(pubPort)).replace("#prvPort", String.valueOf(prvPort)).replace("#ip",ip).split(" ");
		refresh();
		addParameter(prm);
		doIt();
		waitForResult();
	}

	@Override
	protected void handleOutput(String value) {
		System.out.println(value);

	}


	@Override
	protected void handleError(String value) {
		throw new RuntimeException(value);
	}

}
