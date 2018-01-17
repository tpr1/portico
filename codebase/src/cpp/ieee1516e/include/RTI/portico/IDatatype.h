#pragma once
#include "RTI/portico/types/DatatypeClass.h"
#include "RTI/SpecificConfig.h"
#include <string>


class RTI_EXPORT IDatatype
{
public:
	IDatatype(){}

	/**
	* Returns the name of this datatype.
	*/
	virtual std::string getName() const = 0;

	/**
	* Returns the FOM datatype class of this datatype (e.g. Basic, Simple, Enumerated, Array,
	* Fixed Record or Variant).
	*/
	virtual DatatypeClass getDatatypeClass() = 0;
};
