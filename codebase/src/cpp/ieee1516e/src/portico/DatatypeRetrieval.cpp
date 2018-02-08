#include "DatatypeRetrieval.h"
#include "utils\StringUtils.h"

PORTICO1516E_NS_START

DatatypeRetrieval* DatatypeRetrieval::instance = NULL;


DatatypeRetrieval::DatatypeRetrieval()
{
	this->initialized = false;
}


DatatypeRetrieval::~DatatypeRetrieval()
{

}


DatatypeRetrieval* DatatypeRetrieval::get()
{
	// if the instance hasn't been created yet, do so
	if (DatatypeRetrieval::instance == NULL)
		DatatypeRetrieval::instance = new DatatypeRetrieval();

	return DatatypeRetrieval::instance;
}

void DatatypeRetrieval::shutdown()
{
	// delete the runtime (causing the destructor to run)
	delete DatatypeRetrieval::instance;


	// reset the instance, this will allow the runtime to be restarted
	DatatypeRetrieval::instance = NULL;
}

void DatatypeRetrieval::initialize(std::wstring fomString)
{
	string xmlIn = pugi::as_utf8(fomString);
	pugi::xml_parse_result result = this->fomxml.load_string(xmlIn.c_str());

	if (result.status == pugi::xml_parse_status::status_ok)
	{
		this->initialized = true;
	}
}

bool DatatypeRetrieval::isInitialized()
{
	return this->initialized;
}

std::auto_ptr<IDatatype> DatatypeRetrieval::getParameterDatatype( wstring dataTypeName,
																 wstring dataTypeClass)
{
	 
	return  std::auto_ptr<IDatatype>(new BasicType("basic", 4, Endianness::LITTLE));
}



IDatatype* DatatypeRetrieval::getAttributeDatatype(wstring dataTypeName, wstring dataTypeClass)
{ 
	IDatatype* datatype;
	string shortName = StringUtils::toShortString(dataTypeName);

	switch (getDatatypeClassFromName(dataTypeClass))
	{
	case DatatypeClass::BASIC:
		datatype = getBasicTypeFromName(shortName);
		break;
	case DatatypeClass::SIMPLE:
		datatype = getSimpleTypeFromName(shortName);
		break;
	case DatatypeClass::ENUMERATED:
		datatype = getEnumeratedTypeFromName(shortName);
		break;
	case DatatypeClass::ARRAY:
		datatype = getArrayTypeFromName(shortName);
		break;
	case DatatypeClass::FIXEDRECORD:
		datatype = getFixedRecordTypeFromName(shortName);
		break;
	case DatatypeClass::VARIANTRECORD:
		datatype = getVariantRecordTypeFromName(shortName);
		break;
	default :
		datatype = getNaTypeFromName(shortName);
		break;
	}

	// call to cache. Use handle as key ?                    
	return datatype;
}



DatatypeClass DatatypeRetrieval::getDatatypeClassFromName(wstring classTypeName){
	
	if (classTypeName == L"basicData")
	{
		return DatatypeClass::BASIC;
	}
	else if (classTypeName == L"simpleData")
	{
		return DatatypeClass::SIMPLE;
	}
	else if (classTypeName == L"enumeratedData")
	{
		return DatatypeClass::ENUMERATED;
	}
	else if (classTypeName == L"arrayData")
	{
		return DatatypeClass::ARRAY;
	}
	else if (classTypeName == L"fixedRecordData")
	{
		return DatatypeClass::FIXEDRECORD;
	}
	else if (classTypeName == L"variantRecordData")
	{
		return DatatypeClass::VARIANTRECORD;
	}
	else if (classTypeName == L"NA")
	{
		return DatatypeClass::NA;
	} 
}

 
 BasicType* DatatypeRetrieval::getBasicTypeFromName(string name)
{
	// Check if its in the cache
	if (this->basicTypeCache.find(name) == this->basicTypeCache.end())
	{
		// Get the node containng the information for the BASIC type of name
		pugi::xml_node basicTypeNode = getDatatypeNode(DatatypeRetrieval::BASIC, name);

		// Get the parameters from the node
		string typeName = basicTypeNode.attribute("name").as_string();
		int size = basicTypeNode.attribute("size").as_int();

		string endiannessString = basicTypeNode.attribute("endianness").as_string();
		Endianness end = endiannessString == "LITTLE" ? Endianness::LITTLE : Endianness::BIG;

		// Create and cache the new BasicType
		this->basicTypeCache[name] = new BasicType(typeName, size, end);
	}
	
	// return the cached basic type
	return this->basicTypeCache[name];
}





SimpleType* DatatypeRetrieval::getSimpleTypeFromName(string name)
{

	// Check if its in the cache
	if (this->simpleTypeCache.find(name) == this->simpleTypeCache.end())
	{
		// Get the node containng the information for the BASIC type of name
		pugi::xml_node simpleTypeNode = getDatatypeNode(DatatypeRetrieval::SIMPLE, name);
	
		// Get the parameters from the node
		string typeName = simpleTypeNode.attribute("name").as_string();
		string representation = simpleTypeNode.attribute("name").as_string();
		BasicType* basicType = getBasicTypeFromName(representation);
		// Create and cache the new BasicType
		this->simpleTypeCache[name] = new SimpleType(typeName, basicType);
	}

	// return the cached basic type
	return this->simpleTypeCache[name];
}






EnumeratedType* DatatypeRetrieval::getEnumeratedTypeFromName(string name)
{
	// Check if its in the cache
	if (this->enumeratedTypeCache.find(name) == this->enumeratedTypeCache.end())
	{
		std::list<Enumerator*> enumerators;

		// Get the node containng the information for the BASIC type of name
		pugi::xml_node enumeratedTypeNode = getDatatypeNode(DatatypeRetrieval::ENUMERATED, name);

		string representation = enumeratedTypeNode.attribute("representation").as_string();
		BasicType* basicType = getBasicTypeFromName(representation);


		for (pugi::xml_node enumerations = enumeratedTypeNode.first_child(); enumerations; enumerations = enumerations.next_sibling("enumerator"))
		{
			string enumerationName = enumerations.attribute("name").as_string();
			string enumerationValue = enumerations.attribute("values").as_string();

			enumerators.push_back( new Enumerator(enumerationName, enumerationValue) );
		}


		// Create and cache the new BasicType
		this->enumeratedTypeCache[name] = new EnumeratedType(name, basicType, enumerators);
	}

	// return the cached basic type
	return this->enumeratedTypeCache[name];
}

ArrayType* DatatypeRetrieval::getArrayTypeFromName(string name)
{
	return nullptr;
}

FixedRecordType* DatatypeRetrieval::getFixedRecordTypeFromName(string name)
{
	return nullptr;
}

VariantRecordType* DatatypeRetrieval::getVariantRecordTypeFromName(string name)
{
	return nullptr;
}

NaType* DatatypeRetrieval::getNaTypeFromName(string name)
{
	return nullptr;
}

pugi::xml_node DatatypeRetrieval::getDatatypeNode(string type, string name)
{
	 
	string queryString = "//" + type + "[@name ='" + name + "']";
	pugi::xpath_node_set nodeSet = this->fomxml.select_nodes(queryString.c_str());
	
	return nodeSet[0].node();
}


PORTICO1516E_NS_END