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
File: RTI/portico/types/NaType.h
***********************************************************************/

#pragma once

#include "RTI/portico/IDatatype.h" 

 /**
 * This class contains metadata about a FOM Basic data type.
 * <p/>
 * Basic data types represent primitive data types in the FOM and are often the building blocks
 * of more complex data types.
 */
class NaType : public virtual IDatatype
{
 	
public:

	/**
	 * Constructor for NaType with specified name, size and endianness
	 */
    NaType( );
 
	/////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Datatype Interface ////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	/**
	* Returns the name of this datatype.
    * @return The name of this datatype as a string.
	*/
	virtual std::string getName() const;

    /**
    * Returns the FOM datatype class of this datatype (e.g. Basic, Simple, Enumerated, Array,
    * FixedRecord or Variant).
    *
    * @return the DatatypeClass of this record.
    * @see DatatypeClass.
    */
	virtual DatatypeClass getDatatypeClass(); 
 
};
