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
	pugi::xml_document fomxml;
	bool initialized;
	std::map<string, BasicType*> basicTypeCache;
	std::map<string, SimpleType*> simpleTypeCache;
	std::map<string, EnumeratedType*> enumeratedTypeCache;
	std::map<string, ArrayType*> arrayTypeCache;
	std::map<string, FixedRecordType*> fixedRecordTypeCache;
	std::map<string, VariantRecordType*> VariantRecordTypeCache;
	std::map<string, NaType*> NaTypeCache;

	const string BASIC = "basicData";
	const string SIMPLE = "simpleData";
	const string ENUMERATED = "enumeratedData";
	const string ARRAY = "arrayData";
	const string FIXED = "fixedRecordData";
	const string VARIANT = "variantRecordData";
	const string NA = "NA";


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
	static DatatypeRetrieval* get();
	static void shutdown();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
public:
	void initialize(std::wstring fomString);
	bool isInitialized();

	std::auto_ptr<IDatatype> getParameterDatatype( wstring dataTypeName,
												   wstring dataTypeClass); 

	IDatatype* getAttributeDatatype(wstring dataTypeName,
												   wstring dataTypeClass);
 
 private:
	 DatatypeClass getDatatypeClassFromName(wstring classTypeName);
	 BasicType* getBasicTypeFromName(string name);
	 SimpleType* getSimpleTypeFromName(string name);
	 EnumeratedType* getEnumeratedTypeFromName(string name);
	 ArrayType* getArrayTypeFromName(string name);
	 FixedRecordType* getFixedRecordTypeFromName(string name);
	 VariantRecordType* getVariantRecordTypeFromName(string name);
	 NaType* getNaTypeFromName(string name);
	 pugi::xml_node getDatatypeNode(string type, string name);

};

PORTICO1516E_NS_END