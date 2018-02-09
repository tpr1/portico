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
File: RTI/portico/types/Field.h
***********************************************************************/

#pragma once
 
#include "RTI/portico/types/Field.h"  
#include "RTI/portico/IDatatype.h"  


/**
*  
*/
class Field  
{
protected:	 

    std::string name;
    IDatatype*   datatype;

public:

	/**
	 * Constructor for Field with specified name, and Datatype.
	 * 
	 * @param name the name of this enumerator
	 * @param value the value of this enumerator
	 */
    Field(const std::string& name, IDatatype* datatype);
 
    virtual ~Field();


	/////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Field Interface //////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	/**
	* Returns the name of this datatype.
    * @return The name of this datatype as a string.
	*/
	virtual std::string getName() const;

    /**
    * Returns the datatype class of this field.
    *
    * @return the Datatype of this record.
    * @see DatatypeClass.
    */
    virtual IDatatype* getDatatype();
 
    /**
    * Check to see if two Field objects are equal.
    *
    * @return True if they are equal, otherwise false.
    */
    virtual bool operator==(const Field& other);
 
};
