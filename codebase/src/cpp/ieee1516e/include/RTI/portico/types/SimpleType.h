/***********************************************************************
The IEEE hereby grants a general, royalty-free license to copy, distribute,
display and make derivative works from this material, for all purposes,
provided that any use of the material contains the following
attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
Should you require additional information, contact the Manager, Standards
Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
***********************************************************************/
/***********************************************************************
IEEE 1516.1 High Level Architecture Interface Specification C++ API
File: RTI/portico/types/SimpleType.h
***********************************************************************/
#pragma once

#include "RTI/portico/IDatatype.h" 


/**
* This class contains metadata about a FOM Simple data type.
*
* A simple type describes a simple, scalar data item
*/
class SimpleType : public virtual IDatatype
{
protected:

	std::string	name;			/// The name of this datatype
	std::string representation; /// BasicType or DatatypePlaceholder only

public:

	/**
	 * Constructor for SimpleType with specified name and representation
	 *
	 * @param name the name of this data type
	 * @param representation // BasicType or DatatypePlaceholder only
	 */
	SimpleType(const std::string& name, std::string representation);

    //////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////// SimpleType representation /////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    /**
    * Returns the FOM datatype class of this datatype (e.g. Basic, Simple, Enumerated, Array,
    * FixedRecord or Variant).
    *
    * @return The BasicType of this datatype representation.
    * @see BasicType.
    */
    virtual IDatatype* getRepresentation();


	//////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Datatype Interface /////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
    /**
    * Get the type of this datatype as a string.
    *
    * @return The type of this datatype as a string
    */
	virtual std::string getName() const ;

    /**
    * Returns the FOM datatype class of this datatype (e.g. Basic, Simple, Enumerated, Array,
    * FixedRecord or Variant).
    *
    * @return the DatatypeClass of this record.
    * @see DatatypeClass.
    */
	virtual DatatypeClass getDatatypeClass();

};
