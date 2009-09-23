// $Id: RDFUnmarshallerJena.java,v 1.3 2009-09-23 09:21:37 tgambet Exp $
// Author: Damien LEROY.
// (c) COPYRIGHT MIT, ERCIM ant Keio, 2006.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.unicorn.tasklist;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.unicorn.Framework;
import org.w3c.unicorn.tasklist.parameters.Parameter;
import org.w3c.unicorn.tasklist.parameters.Value;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author Damien LEROY
 * 
 */
public class RDFUnmarshallerJena implements RDFUnmarshaller {

	private static final Log logger = LogFactory.getLog(RDFUnmarshallerJena.class);

	private static final Model MODEL = ModelFactory.createDefaultModel();

	private static final String UCN_NAMESPACE = "http://www.w3.org/unicorn#";

	private static Resource RESOURCE_TASK = null;

	private static Property PROPERTY_DESCRIPTION = null;

	private static Property PROPERTY_HASPARAMETER = null;

	private static Property PROPERTY_HASVALUE = null;

	private static Property PROPERTY_LONGNAME = null;

	private static Property PROPERTY_PARAMETER = null;

	private static Property PROPERTY_REFERENCE = null;

	private static Property PROPERTY_TYPE = null;

	private static Property PROPERTY_VALUE = null;
	
	private static Property PROPERTY_DEFAULT = null;

	private Tasklist mapOfTask = null;

	private Model aModel = null;

	/**
	 * Default constructor.
	 * 
	 */
	public RDFUnmarshallerJena() {
		logger.trace("Constructor");
		this.aModel = ModelFactory.createDefaultModel();
	}

	/**
	 * Adds a Model with the given URL to this aModel.
	 * 
	 * @param aURL
	 *            The URL to add.
	 */
	public void addURL(final URL aURL) throws IOException {
		logger.trace("addURL");
		logger.debug("URL : " + aURL + ".");
		final Model aModel = ModelFactory.createDefaultModel();
		aModel.read(aURL.openStream(), null);
		this.aModel.add(aModel);
	}

	/**
	 * Adds a name (with its language) to the given task.
	 * 
	 * @param aTask
	 *            The task to name.
	 * @param aLiteral
	 *            The name of the task.
	 */
	private void addLongName(final Task aTask, final Literal aLiteral) {
		logger.debug("LongName lang:"
				+ aLiteral.getLanguage() + " value:" + aLiteral.getString()
				+ ".");
		aTask.addLongName(aLiteral.getLanguage(), aLiteral.getString());
	}

	/**
	 * Adds a description (with its language) to the given task.
	 * 
	 * @param aTask
	 *            The task to describe.
	 * @param aLiteral
	 *            The description of the task.
	 */
	private void addDescription(final Task aTask, final Literal aLiteral) {
		logger.debug("Description lang:"
				+ aLiteral.getLanguage() + " value:" + aLiteral.getString()
				+ ".");
		aTask.addDescription(aLiteral.getLanguage(), aLiteral.getString());
	}

	/**
	 * Adds a name (with its language) to the given parameter.
	 * 
	 * @param aTask
	 *            The parameter to name.
	 * @param aLiteral
	 *            The name of the parameter.
	 */
	private void addLongName(final Parameter aParameter, final Literal aLiteral) {
		logger.debug("Parameter long name lang:"
				+ aLiteral.getLanguage() + " value:" + aLiteral.getString()
				+ ".");
		aParameter.addLongName(aLiteral.getLanguage(), aLiteral.getString());
	}

	/**
	 * Adds a name (with its language) to the given value.
	 * 
	 * @param aTask
	 *            The value to name.
	 * @param aLiteral
	 *            The name of the value.
	 */
	private void addLongName(final Value aValue, final Literal aLiteral) {
		logger.debug("Value long name lang:"
				+ aLiteral.getLanguage() + " value:" + aLiteral.getString()
				+ ".");
		aValue.addLongName(aLiteral.getLanguage(), aLiteral.getString());
	}

	/**
	 * Adds a value to the given parameter.
	 * 
	 * @param aParameter
	 *            The parameter to consider.
	 * @param aValue
	 *            The value to add.
	 */
	private void addValue(final Parameter aParameter, final Resource aValue) {
		final String sValue = aValue.getProperty(
				RDFUnmarshallerJena.PROPERTY_REFERENCE).getLiteral().getString();
		final Value oValue = aParameter.getValue(sValue);
		if (null == oValue) {
			logger.warn("Value " + sValue
					+ " not found in parameter " + aParameter.getName() + ".");
			return;
		}
		logger.debug("Parameter value  : " + sValue + ".");
		// find and add longName of the Value
		for (final StmtIterator siLongName = aValue
				.listProperties(RDFUnmarshallerJena.PROPERTY_LONGNAME); siLongName
				.hasNext();) {
			final Literal lLongName = siLongName.nextStatement().getLiteral();
			this.addLongName(oValue, lLongName);
		} // find and add longName of the Value
	}

	/**
	 * Adds a parameter to the given task.
	 * 
	 * @param aTask
	 *            The task to consider.
	 * @param aParameter
	 *            The parameter to add.
	 */
	private void addParameter(final Task aTask, final Resource aParameter) {
		final String sParameterReference = aParameter.getProperty(
				RDFUnmarshallerJena.PROPERTY_REFERENCE).getLiteral()
				.getString();
		final Parameter oParameter = aTask.getMapOfParameter().get(
				sParameterReference);
		if (null == oParameter) {
			logger.warn("Parameter " + sParameterReference
					+ " not found in task " + aTask.getID() + ".");
			return;
		}
		logger.debug("Parameter : " + sParameterReference
				+ ".");
		// find and add longName of the Parameter
		for (StmtIterator siLongName = aParameter
				.listProperties(RDFUnmarshallerJena.PROPERTY_LONGNAME); siLongName
				.hasNext();) {
			final Literal lLongName = siLongName.nextStatement().getLiteral();
			this.addLongName(oParameter, lLongName);
		} // find and add longName of the Parameter
		// find and add value of the Parameter
		for (final StmtIterator siValue = this.aModel.listStatements(
				aParameter, RDFUnmarshallerJena.PROPERTY_HASVALUE,
				(RDFNode) null); siValue.hasNext();) {
			final Resource aValue = (Resource) siValue.nextStatement()
					.getObject();
			if (null == aValue) {
				logger.error("Resource value == null.");
				continue;
			}
			this.addValue(oParameter, aValue);
		} // find and add value of the Parameter
	}

	/**
	 * Adds a task to this object.
	 * 
	 * @param aTask
	 *            The task to add.
	 * @throws Exception
	 */
	private void addTask(final Resource aTask) throws Exception {
		final Statement aReference = aTask
				.getProperty(RDFUnmarshallerJena.PROPERTY_REFERENCE);
		final Task oTask = this.mapOfTask.get(aReference.getLiteral()
				.getString());
		if (oTask == null) {
			// TODO creer et initialiser une Task si les informations peuvent
			// TODO etre mise uniquement dans le fichier rdf
			Framework.logger.warn("> Found unused metadata for: " + aReference.getLiteral().getString());
			return;
		} else {
			Framework.logger.debug("> Found metadata for: " + aReference.getLiteral().getString());
			Statement isDefault = aTask.getProperty(RDFUnmarshallerJena.PROPERTY_DEFAULT);
			if (isDefault != null && isDefault.getLiteral().toString().equals("true")) {
				this.mapOfTask.setDefaultTaskId(aReference.getLiteral().getString());
				Framework.logger.debug(">> This is the default task.");
			}
		}
		logger.debug("Reference : "
				+ aReference.getObject().toString() + ".");
		// find and add longName of the task
		for (final StmtIterator siLongName = aTask
				.listProperties(RDFUnmarshallerJena.PROPERTY_LONGNAME); siLongName
				.hasNext();) {
			final Literal lLongName = siLongName.nextStatement().getLiteral();
			this.addLongName(oTask, lLongName);
		} // find and add longName of the task
		// find and add description of the task
		for (final StmtIterator siDescription = aTask
				.listProperties(RDFUnmarshallerJena.PROPERTY_DESCRIPTION); siDescription
				.hasNext();) {
			final Literal lDescription = siDescription.nextStatement()
					.getLiteral();
			this.addDescription(oTask, lDescription);
		} // find and add description of the task
		// find and add Parameter of the task
		for (final StmtIterator siParameter = this.aModel.listStatements(aTask,
				RDFUnmarshallerJena.PROPERTY_HASPARAMETER, (RDFNode) null); siParameter
				.hasNext();) {
			final Resource aParameter = (Resource) siParameter.nextStatement()
					.getObject();
			if (null == aParameter) {
				logger.error("Resource parameter == null.");
				continue;
			}
			this.addParameter(oTask, aParameter);
		} // find and add Parameter of the task
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.unicorn.util.Unmarshaller#unmarshal(java.net.URL)
	 */
	public void unmarshal() throws Exception {
		logger.trace("unmarshal");

		// find and add task
		for (final StmtIterator siTask = this.aModel.listStatements(null,
				RDFUnmarshallerJena.PROPERTY_TYPE,
				RDFUnmarshallerJena.RESOURCE_TASK); siTask.hasNext();) {
			final Resource aTask = siTask.nextStatement().getSubject();
			this.addTask(aTask);
		} // find and add task
		logger.trace("End.");
	}

	/**
	 * @return Returns the tasks.
	 */
	public Tasklist getMapOfTask() {
		return this.mapOfTask;
	}

	/**
	 * @param mapOfTask
	 *            The tasks to set.
	 */
	public void setMapOfTask(final Tasklist mapOfTask) {
		this.mapOfTask = mapOfTask;
	}

	public static Resource getRESOURCE_TASK() {
		return RESOURCE_TASK;
	}

	public static void setRESOURCE_TASK(Resource rESOURCETASK) {
		RESOURCE_TASK = rESOURCETASK;
	}

	public static Property getPROPERTY_DESCRIPTION() {
		return PROPERTY_DESCRIPTION;
	}

	public static void setPROPERTY_DESCRIPTION(Property pROPERTYDESCRIPTION) {
		PROPERTY_DESCRIPTION = pROPERTYDESCRIPTION;
	}

	public static Property getPROPERTY_HASPARAMETER() {
		return PROPERTY_HASPARAMETER;
	}

	public static void setPROPERTY_HASPARAMETER(Property pROPERTYHASPARAMETER) {
		PROPERTY_HASPARAMETER = pROPERTYHASPARAMETER;
	}

	public static Property getPROPERTY_HASVALUE() {
		return PROPERTY_HASVALUE;
	}

	public static void setPROPERTY_HASVALUE(Property pROPERTYHASVALUE) {
		PROPERTY_HASVALUE = pROPERTYHASVALUE;
	}

	public static Property getPROPERTY_LONGNAME() {
		return PROPERTY_LONGNAME;
	}

	public static void setPROPERTY_LONGNAME(Property pROPERTYLONGNAME) {
		PROPERTY_LONGNAME = pROPERTYLONGNAME;
	}

	public static Property getPROPERTY_PARAMETER() {
		return PROPERTY_PARAMETER;
	}

	public static void setPROPERTY_PARAMETER(Property pROPERTYPARAMETER) {
		PROPERTY_PARAMETER = pROPERTYPARAMETER;
	}

	public static Property getPROPERTY_REFERENCE() {
		return PROPERTY_REFERENCE;
	}

	public static void setPROPERTY_REFERENCE(Property pROPERTYREFERENCE) {
		PROPERTY_REFERENCE = pROPERTYREFERENCE;
	}

	public static Property getPROPERTY_TYPE() {
		return PROPERTY_TYPE;
	}

	public static void setPROPERTY_TYPE(Property pROPERTYTYPE) {
		PROPERTY_TYPE = pROPERTYTYPE;
	}

	public static Property getPROPERTY_VALUE() {
		return PROPERTY_VALUE;
	}

	public static void setPROPERTY_VALUE(Property pROPERTYVALUE) {
		PROPERTY_VALUE = pROPERTYVALUE;
	}

	public static Model getModel() {
		return MODEL;
	}

	public static String getUcnNamespace() {
		return UCN_NAMESPACE;
	}

	public static Property getPROPERTY_DEFAULT() {
		return PROPERTY_DEFAULT;
	}

	public static void setPROPERTY_DEFAULT(Property pROPERTYDEFAULT) {
		PROPERTY_DEFAULT = pROPERTYDEFAULT;
	}

	
	
}
