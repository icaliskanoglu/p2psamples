package com.isc.sample.p2p.samples;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class ProcessMaster {

	private static final Logger		LOGGER		= LoggerFactory.getLogger(ProcessMaster.class);
	private final ArrayList<String> parameters;

	protected BufferedReader processOutput;

	protected BufferedReader processError;

	protected BufferedWriter processInput;

	protected Process p;

	private volatile boolean		lock;

	private Thread timeBomb	= null;

	private long					duration	= 0;

	protected ProcessMaster() {
		this.parameters = new ArrayList<String>();
	}

	protected void refresh() {
		this.parameters.clear();
	}

	protected ProcessMaster(ArrayList<String> parameters) {
		this();
		this.parameters.addAll(parameters);
	}

	protected void addParameter(String... params) {
		for (String param : params) {
			if (!parameters.contains(param)) {
				parameters.add(param);
			}
		}
	}

	protected void addParameters(List<String> params) {
		parameters.addAll(params);
	}

	public long getDuration() {
		return duration;
	}

	private void handleOutput() {
		new Thread() {

			@Override
			public void run() {
				String buf = "";
				try {
					while ((buf = processOutput.readLine()) != null) {
						handleOutput(buf);
					}
				}
				catch (IOException e) {
					LOGGER.error("EROOR: ", e);
				}
			}
		}.start();
	}

	/**
	 * Programdan gelen output Stream'in islenmesi için hazirlanmis method
	 *
	 * @param value
	 *            - gelen satir degeri
	 */
	abstract protected void handleOutput(String value);

	private void handleError() {
		new Thread() {

			@Override
			public void run() {
				String buf = "";
				try {
					while ((buf = processError.readLine()) != null) {
						handleError(buf);
					}
				}
				catch (IOException e) {
					LOGGER.error("EROOR: ", e);
				}
			}
		}.start();
	}

	/**
	 * Uygulamadan gelen error stream'in handle edilmesi icin.
	 *
	 * @param value
	 *            - gelen satir
	 */
	abstract protected void handleError(String value);

	/**
	 * uygulamaya mesaj gondermek icin.
	 *
	 * @param command
	 */
	protected void sendCommand(String command) {
		String cmd = command + "\n";
		try {
			processInput.write(cmd);
			processInput.flush();
		}
		catch (Exception e) {
			LOGGER.error("EROOR: ", e);
			processInput = null;
			processInput = null;

		}
	}

	protected void setTimeBomb(final long timeout) {
		timeBomb = new Thread() {

			@Override
			public void run() {
				try {
					Thread.sleep(timeout);
					p.destroy();
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};

	}

	protected void ignition() {
		parameters.add(0,"echo 7889 | sudo -S");
		parameters.add(1,getDefaultCommand());
		String[] cmd = {"/bin/bash","-c", StringUtils.join(parameters, " ")};

		ProcessBuilder pb = new ProcessBuilder(cmd);
		p = null;
		try {
			p = pb.start();
			timeBomb.start();
			processOutput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			processError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			processInput = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			handleOutput();
			handleError();

		}
		catch (Exception e) {
			LOGGER.error("EROOR: ", e);
			p.destroy();
		}
		try {
			lock = true;
			processStarted();
			duration = new Date().getTime();
			p.waitFor();
		}
		catch (InterruptedException e) {
			LOGGER.error("EROOR: ", e);
			p.destroy();

		}
		finally {
			lock = false;
			duration = (new Date().getTime()) - duration;
			processFinished();
			if (timeBomb != null) {
				timeBomb.stop();
			}
		}
	}

	public void doIt() {
		setTimeBomb(10000);
		ignition();
	}

	protected void waitForResult() {
		while (true) {
			try {
				// Thread.sleep(100);
				// if (!result.trim().equalsIgnoreCase("")) {
				// break;
				// }
				p.waitFor();
				break;
			}
			catch (Exception e) {
				LOGGER.error("EROOR: ", e);
			}
		}
	}

	protected void processFinished() {
	}

	protected void processStarted() {
	}

	abstract protected String getDefaultCommand();
}