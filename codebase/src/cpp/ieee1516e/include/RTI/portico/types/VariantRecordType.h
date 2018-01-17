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
File: RTI/portico/types/VariantRecordType.h
***********************************************************************/

#pragma once

#include "RTI/portico/IDatatype.h"
#include "RTI/portico/types/Alternative.h"  
#include <list>
  /**
  * This class contains metadata about a FOM Simple data type.
  * <p/>
  * A variant record datatype represents a discriminated union of types.
  */
class VariantRecordType : public virtual IDatatype
{
protected:	 

	std::string	            name;		            /// The name of this datatype
    IDatatype*	            discriminantDatatype;	/// The descriminant Datatype 
    std::string	            discriminantName;       /// The descriminant datatype name
    std::list<Alternative>  alternatives;
	
public:

	/**
	 * Constructor for VariantRecordType 
	 * 
	 * @param name the name of this data type
	 * @param discriminantName the name of the descriminant datatype
	 * @param discriminantDatatype the descriminant Datatype from the DataClassType enumerations.
     * @param alternatives the alternative datatypes
	 */
    VariantRecordType(const std::string& name,
              const std::string& discriminantName, 
              IDatatype* discriminantDatatype,
              std::list<Alternative>  alternatives);

    ~VariantRecordType();
 
    /////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////// VariantRecordType Interface ////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Get the descriminate datatype name.
	 * 
	 * @return the discriminant name as a string
	 */
	virtual std::string getDiscriminateName() const;

    /**
    * Get the descriminate datatype.
    *
    * @return the discriminant datatype 
    */
    virtual IDatatype* getDiscriminateDatatype() const;

    /**
    * Set the descriminate datatype.
    *
    * @param the discriminant datatype
    */
    virtual void setDiscriminateDatatype(IDatatype* discriminantDatatype);
    /**
    * Get the Alternate.
    *
    * @return the the Alternate for this variant record
    */
    virtual std::list<Alternative> getAlternatives() const;


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
