#pragma once

#include "jni/JavaRTI.h"
#include "pugixml/pugixml.hpp"
#include <string>

#include "RTI\portico\types\BasicType.h"
#include "RTI\portico\types\EnumeratedType.h"
#include "RTI\portico\types\SimpleType.h"
#include "RTI\portico\types\ArrayType.h"
#include "RTI\portico\types\FixedRecordType.h"
#include "RTI\portico\types\VariantRecordType.h"
#include "RTI\portico\types\NaType.h"

PORTICO1516E_NS_START

// forward declaration of JavaRTI to resolve circular-dependency
class JavaRTI;



class DatatypeRetrieval
{

	// give JavaRTI access to our bits
	friend class JavaRTI;

	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
private:
	static DatatypeRetrieval* instance;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
private:
	pugi::xml_document fomxml;						/// Hold the FOM in xml data structure.
	bool initialized;								/// True if the FOM has been initialized.
	std::map<string, IDatatype*> typeCache;			/// Stores the cache of all retrieved datatypes
	std::map<string, Enumerator*> enumeratorCache;	/// Stores the cache of all recieved enumerators

	static const string BASIC;
	static const string SIMPLE;
	static const string ENUMERATED;
	static const string ARRAY;
	static const string FIXED;
	static const string VARIANT;
	static const string NA;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
private:
	DatatypeRetrieval();

public:
	~DatatypeRetrieval();
	

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
public:
	/***
	* Get the instance of the datatypeRetrieval object.
	* @return the handle to the DatatypeRetrieval object
	*/
	static DatatypeRetrieval* get();

	/**
	* Clean up the resources for the datatypeRetrieval object.
	*/
	static void shutdown();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
public:
	/***
	* Initialize the FOM xml object for the datatype retrieval object.
	* @param The fom object in xml as a widestring
	*/
	void initialize(std::wstring fomString);

	/**
	* Check to see if the FOM has been initialized.
	* @return True if the fom has been initialized, otherwise false.
	*/
	bool isInitialized();

	/**
	* Get the parameter datatype given the name of a datatype.
	* @param dataTypeName The name of the class being requested.
	* @return The pointer to the datatype requested.
	*/
	IDatatype* getParameterDatatype(string dataTypeName);
	
	/**
	* Get the attribute datatype given the name of a datatype.
	* @param dataTypeName The name of the class being requested.
	* @return The pointer to the datatype requested.
	*/
	IDatatype* getAttributeDatatype(string dataTypeName);
 
 private:
	 /**
	 * Using the FOM get the datatypeClass for the datatype with classTypeName.
	 * @param classTypeName The name of the class the type is being requested for.
	 * @return The datatypeClass of the requested class.
	 * @see DatatypeClass
	 */
	 DatatypeClass getDatatypeClassFromName( wstring classTypeName );

	 /**
	 * Get a datatype given the name of a class. This is shared by both
	 * parameter and attribute requests.
	 * @param dataTypeName The name of the class being requested.
	 * @return The pointer to the datatype requested.
	 * @see DatatypeClass
	 * @see IDatatype
	 * @see BasicType
	 * @see SimpleType
	 * @see ArrayType
	 * @see EnumeratedType
	 * @see FixedRecordType
	 * @see VariantRecordType
	 */
	 IDatatype* getDatatype(string dataTypeName);

	 /**
	 * Create a BasicType from information stored in the FOM
	 * @param dataNode The XML node that contains the information required 
	 *				   to build the BasicType.
	 * @return The pointer to the datatype requested.
	 * @see DatatypeClass
	 * @see IDatatype
	 * @see BasicType
	 */
	 IDatatype* getBasicType( pugi::xml_node dataNode );

	 /**
	 * Create a SimpleType from information stored in the FOM
	 * @param dataNode The XML node that contains the information required
	 *				   to build the SimpleType.
	 * @return The pointer to the datatype requested.
	 * @see DatatypeClass
	 * @see IDatatype
	 * @see SimpleType
	 */
	 IDatatype* getSimpleType( pugi::xml_node dataNode );

	 /**
	 * Create a EnumeratedType from information stored in the FOM
	 * @param dataNode The XML node that contains the information required
	 *				   to build the EnumeratedType.
	 * @return The pointer to the datatype requested.
	 * @see DatatypeClass
	 * @see IDatatype
	 * @see EnumeratedType
	 */
	 IDatatype* getEnumeratedType( pugi::xml_node dataNode );

	 /**
	 * Create a ArrayType from information stored in the FOM
	 * @param dataNode The XML node that contains the information required
	 *				   to build the ArrayType.
	 * @return The pointer to the datatype requested.
	 * @see DatatypeClass
	 * @see IDatatype
	 * @see ArrayType
	 */
	 IDatatype* getArrayType( pugi::xml_node dataNode );

	 /**
	 * Create a FixedRecordType from information stored in the FOM
	 * @param dataNode The XML node that contains the information required
	 *				   to build the FixedRecordType.
	 * @return The pointer to the datatype requested.
	 * @see DatatypeClass
	 * @see IDatatype
	 * @see FixedRecordType
	 */
	 IDatatype* getFixedRecordType( pugi::xml_node dataNode );

	 /**
	 * Create a VariantRecordType from information stored in the FOM
	 * @param dataNode The XML node that contains the information required
	 *				   to build the VariantRecordType.
	 * @return The pointer to the datatype requested.
	 * @see DatatypeClass
	 * @see IDatatype
	 * @see VariantRecordType
	 */
	 IDatatype* getVariantRecordType( pugi::xml_node dataNode );

	 /**
	 * Create a NaType from information stored in the FOM
	 * @param dataNode The XML node that contains the information required
	 *				   to build the NaType.
	 * @return The pointer to the datatype requested.
	 * @see DatatypeClass
	 * @see IDatatype
	 * @see NaType
	 */
	 IDatatype* getNaType( pugi::xml_node dataNode );

	 /**
	 * Get the XML FOM node that contains all the information on the 
	 * requested datatype.
	 * @param name The name of the class being requested.
	 * @return The XML FOM node for the datatype requested.
	 */
	 pugi::xml_node getDatatypeNode( string name );

	 /**
	 * Create and cache an enumeratedType given the name of one of it's
	 * child enumerations. 
	 * @param name The name of the enumerator that we want the parent created.
	 * @return True if we initialized the parent type, otherwise false..
	 */
	 bool initEnumeratedTypeByEnumerator(string name);
	 
	 /**
	 * Create and cache an enumerator for use in VariantRecordTypes. 
	 * @param name The name of the enumerator.
	 * @param value The value of the enumerator.
	 * @return True if we initialized the parent type, otherwise false..
	 */
	 Enumerator* createEnumeratorAndCache(string name, string value);

};

PORTICO1516E_NS_END