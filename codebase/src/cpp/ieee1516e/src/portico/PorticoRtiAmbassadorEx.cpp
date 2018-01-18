#include "rtiamb/PorticoRtiAmbassador.h"
#include "RTI/portico/types/BasicType.h"
#include "jni/JniUtils.h"


PORTICO1516E_NS_START

	std::auto_ptr<IDatatype> PorticoRtiAmbassador::getAttributeDatatype( ObjectClassHandle whichClass,
																		 AttributeHandle theHandle)
		throw ( InteractionParameterNotDefined,
				InvalidParameterHandle,
				InvalidInteractionClassHandle,
				FederateNotExecutionMember,
				NotConnected,
				RTIinternalError )
	{

		// call to cache. Use handle as key ?                    
        return  std::auto_ptr<IDatatype>( new BasicType("name", 4, Endianness::LITTLE ));
	}


	std::auto_ptr<IDatatype> PorticoRtiAmbassador::getParameterDatatype( InteractionClassHandle whichClass,
																		 ParameterHandle theHandle)
		throw ( InteractionParameterNotDefined,
				InvalidParameterHandle,
				InvalidInteractionClassHandle,
				FederateNotExecutionMember,
				NotConnected,
				RTIinternalError )
	{
        // call to cache. Use handle as key ?

		return std::auto_ptr<IDatatype>(new BasicType("name", 4, Endianness::LITTLE) );
	}

	std::wstring PorticoRtiAmbassador::getFom()
		throw ( NotConnected,
				RTIinternalError )
    {
        // Get active environment
        JNIEnv* jnienv = this->javarti->getJniEnvironment();

        // call the method andConvert to a string		
		wstring fomString = JniUtils::toWideString(jnienv, (jstring)jnienv->CallObjectMethod(javarti->jproxy, javarti->GET_FOM));

		return fomString;
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
