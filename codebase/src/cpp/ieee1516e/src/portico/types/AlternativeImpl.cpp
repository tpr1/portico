#include "RTI/portico/types/Alternative.h" 
 
Alternative::Alternative(const std::string& name, IDatatype* datatype, std::list<IEnumerator*> enumerators)
{
    this->name = name;
    this->datatype = datatype;
    this->enumerators = enumerators; 
}

Alternative::~Alternative()
{

}

IDatatype* Alternative::getDatatype()
{
    return this->datatype;
}

void Alternative::setDatatype(IDatatype *datatype)
{
    this->datatype = datatype;
}

std::list<IEnumerator*>& Alternative::getEnumerators()
{
    return this->enumerators;
}

void Alternative::setEnumerators(std::list<IEnumerator*>& enumerators)
{
    this->enumerators = enumerators;
}

std::string Alternative::getName() const
{
    return this->name;
}

 