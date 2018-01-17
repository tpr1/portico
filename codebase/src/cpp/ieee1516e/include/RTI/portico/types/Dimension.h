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
File: RTI/portico/types/BasicType.h
***********************************************************************/
#pragma once


class Dimension {
public:
    const static int CARDINALITY_DYNAMIC = -1; 

private:
    int lowerCardinality;
    int upperCardinality;

public:
    Dimension(int cardinality);
    Dimension(int lower, int upper);
    virtual ~Dimension();

    virtual int getCardinalityLowerBound();
    virtual int getCardinalityUpperBound();
    virtual bool isCardinalityDynamic();

};