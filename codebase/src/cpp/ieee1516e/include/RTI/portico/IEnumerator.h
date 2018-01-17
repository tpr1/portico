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
File: RTI/portico/types/IEnumerator.h
***********************************************************************/
#pragma once

#include "RTI/portico/types/DatatypeClass.h"
#include "RTI/SpecificConfig.h"
#include <string>


/**
* Describes a possible value of an {@link EnumeratedType}.
* <p/>
* According to the specification, the value of an {@link EnumeratedType} can be any
* {@link BasicType}. As a result, we'll use the {@link Number} class to represent it, as that is
* large enough to represent all basic types.
* <p/>
* <b>Note</b> An interface is required for enumerators due to the working assumption that datatypes
* may be imported in an arbitrary order. {@link Alternative} entries reference enumerators
* and at parse/merge time we must work on the assumption that the {@link EnumeratedType} of the
* discriminant not been imported yet. The interface allows the parser/merger to insert a
* placeholder until all datatypes have been imported and the {@link Linker} is able to resolve
* them to their complete representation
*/
class RTI_EXPORT IEnumerator
{
public:

    /**
    * Returns the name of the enumerator constant
    */
    virtual std::string getName() const = 0;

    /**
    * Returns the value of the enumerator constant
    */
    virtual std::string getValue() = 0;

public:

    IEnumerator(){}
    virtual ~IEnumerator();


};
