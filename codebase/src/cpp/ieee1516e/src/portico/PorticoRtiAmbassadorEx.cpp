#include "portico/PorticoRtiAmbassadorEx.h"
#include "RTI/portico/types/BasicType.h"
#include "jni/JniUtils.h"


//ObjectModel PorticoRtiAmbassadorEx::getFom()
//{
//	return ObjectModel();
//}
PORTICO1516E_NS_START

	PorticoRtiAmbassadorEx::PorticoRtiAmbassadorEx() : PorticoRtiAmbassador()
	{

	}

	PorticoRtiAmbassadorEx::~PorticoRtiAmbassadorEx()
	{

	}


	std::auto_ptr<IDatatype> PorticoRtiAmbassadorEx::getAttributeDatatype(ObjectClassHandle whichClass,
												                          AttributeHandle theHandle)
	{

		// call to cache. Use handle as key ?
                    
        return  std::auto_ptr<IDatatype>(new BasicType("name", 4, Endianness::LITTLE));
	}


	std::auto_ptr<IDatatype>  PorticoRtiAmbassadorEx::getParameterDatatype(InteractionClassHandle whichClass,
													                       ParameterHandle theHandle)
	{
        // call to cache. Use handle as key ?

		return std::auto_ptr<IDatatype>(new BasicType("name", 4, Endianness::LITTLE) );
	}

    std::string PorticoRtiAmbassadorEx::getFom()
    {
        // Get active environment
        JNIEnv* jnienv = this->javarti->getJniEnvironment();

        // call the method
        jstring fom = (jstring)jnienv->CallObjectMethod(javarti->jproxy,
            javarti->GET_FOM );

        return JniUtils::toString(jnienv, fom);
    }

PORTICO1516E_NS_END


IEEE1516E_NS_START

RTIambassadorEx::RTIambassadorEx()
{

}

RTIambassadorEx::~RTIambassadorEx()
{

}
IEEE1516E_NS_END
