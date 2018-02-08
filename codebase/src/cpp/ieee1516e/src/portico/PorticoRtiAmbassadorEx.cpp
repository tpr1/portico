#include "rtiamb/PorticoRtiAmbassador.h"
#include "RTI\portico\types\BasicType.h"
#include "portico/DatatypeRetrieval.h"
#include "jni/JniUtils.h"


PORTICO1516E_NS_START

	IDatatype* PorticoRtiAmbassador::getAttributeDatatype( ObjectClassHandle whichClass,
																		 AttributeHandle theHandle)
		throw ( InteractionParameterNotDefined,
				InvalidParameterHandle,
				InvalidInteractionClassHandle,
				FederateNotExecutionMember,
				NotConnected,
				RTIinternalError )
	{

		JNIEnv* jnienv = this->javarti->getJniEnvironment();

		// Convert handles for call
		jint classHandle = JniUtils::fromHandle(whichClass);
		jint attributeHandle = JniUtils::fromHandle(theHandle);

		// Get the class type / name token pair
		jobjectArray info = (jobjectArray)jnienv->CallObjectMethod(javarti->jproxy,
														 javarti->GET_ATTRIBUTE_DATATYPE,
														 classHandle,
														 attributeHandle);
	 
		// Create the string set from the 
		set<wstring> details =  JniUtils::toWideStringSet(jnienv, info);
		
		DatatypeRetrieval* databutler = DatatypeRetrieval::get();
		if (!databutler->isInitialized())
		{
			databutler->initialize(this->getFom());
		}

		return databutler->getAttributeDatatype((*details.begin()), (*details.rbegin())) ;
	}


	IDatatype* PorticoRtiAmbassador::getParameterDatatype( InteractionClassHandle whichClass,
																		 ParameterHandle theHandle)
		throw ( InteractionParameterNotDefined,
				InvalidParameterHandle,
				InvalidInteractionClassHandle,
				FederateNotExecutionMember,
				NotConnected,
				RTIinternalError )
	{
        // call to cache. Use handle as key ?

		//this->getParameterName()
		// Get active environment
		


		return new BasicType("name", 4, Endianness::LITTLE);
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
