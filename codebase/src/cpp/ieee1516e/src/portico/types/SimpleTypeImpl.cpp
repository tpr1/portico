#pragma once

#include "RTI/portico/types/SimpleType.h"


SimpleType::SimpleType(const std::string& name, std::string representation)
{
    this->name = name;
    this->representation = representation;
}

std::string SimpleType::getName() const
{
	return this->name;
}

DatatypeClass SimpleType::getDatatypeClass()
{
	return DatatypeClass::SIMPLE;
}

IDatatype* SimpleType::getRepresentation()
{
    return this;       
}

 
