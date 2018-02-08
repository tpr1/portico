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
File: RTI/portico/types/EnumeratedType.h
***********************************************************************/
#pragma once

#include "RTI/portico/IDatatype.h" 
#include "RTI/portico/types/Enumerator.h"
#include "RTI/portico/types/BasicType.h"
#include <list> 

/**
* This class contains metadata about a FOM Array data type.
* <p/>
* An array data type is a homogenous collection of a specified data type. Array data types may
* be single or multi-dimensional, and each dimension may have a fixed or dynamic cardinality.
*/
class EnumeratedType : public virtual IDatatype
{
protected:
	 
	std::string	            name;			/// The name of this datatype
	BasicType*	            representation;	/// The size of this datatype
    std::list<Enumerator*>  enumerators;
	
public:

    /**
    * Create a EnumerationType from a list of enumeration strings.
    *
    * @param name the name of the EnumerationType
    * @param representation the type of data that will be stored in instances of this array
    * @param enumerators A list of enumerator names (that will be given default int values ??)
    */
	EnumeratedType(const std::string& name, BasicType* representation, std::list<std::string> enumerators);
      
    /**
    * Create a EnumerationType from a list of enumerations.
    *
    * @param name the name of the EnumerationType
    * @param representation the type of data that will be stored in instances of this array
    * @param enumerators A list of enumerator names (that will be given default int values ??)
    */
	EnumeratedType(const std::string& name, BasicType* representation, std::list<Enumerator*> enumerators);
 
    virtual ~EnumeratedType();

    /////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////// EnumerationType Interface //////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    /**
    * Get the IDatatype Representation of this EnumerationType. 
    *
    * @return The IDatatype representation of this EnumerationType.
    * @see IDatatype
    */
	virtual BasicType* getRepresentation();

    /**
    * Set the representation associated of this EnumerationType. 
    *
    * @param representation The IDatatype associated with this EnumerationType.
    * @see IDatatype
    */
	virtual void setRepresentation(BasicType *representation);

    /**
    * Get the Enumerators associated with this EnumerationType. 
    *
    * @return The Enumerators associated with this EnumerationType as a list.
    * @see IDatatype
    * @see Enumeration
    */
    virtual std::list<Enumerator*>& getEnumerators();
 
    /**
    * Check to see if two EnumeratedTypes are equal.
    *
    * @return True if they are equal, otherwise false.
    */
    virtual bool operator==(const EnumeratedType& other);


private:

    /**
    * Take a list of strings and create enumerations from them.
    *
    * @param constants A list of string name values.
    * @return A list of Enumerator objects.
    */
    virtual std::list<Enumerator*> createEnumeratorsFromNames(const std::list<std::string>& constants);

	/////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Datatype Interface ////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
public:
	/**
	* Returns the name of this datatype.
    *
    * @return The name of this datatype as a string.
	*/
	virtual std::string getName() const ;

    /**
    * Returns the FOM datatype class of this datatype (e.g. Basic, Simple, Enumerated, Array,
    * FixedRecord or Variant).
    *
    * @return the DatatypeClass of this record.
    * @see DatatypeClass.
    */
	virtual DatatypeClass getDatatypeClass() ; 

};
