#pragma once

#include <string>
#include "rtiamb/PorticoRtiAmbassador.h"
#include "RTI/portico/RTIambassadorEx.h"

PORTICO1516E_NS_START

class PorticoRtiAmbassadorEx : public PorticoRtiAmbassador, public rti1516e::RTIambassadorEx {

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
public:
	PorticoRtiAmbassadorEx();
	~PorticoRtiAmbassadorEx();

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
public:

	/*ObjectModel getFom();*/

	std::auto_ptr<IDatatype> getAttributeDatatype(ObjectClassHandle whichClass,
								  AttributeHandle theHandle)
		throw(AttributeNotDefined,
			InvalidAttributeHandle,
			InvalidObjectClassHandle,
			FederateNotExecutionMember,
			NotConnected,
			RTIinternalError);


	std::auto_ptr<IDatatype> getParameterDatatype(InteractionClassHandle whichClass,
								   ParameterHandle theHandle)
		throw (InteractionParameterNotDefined,
			InvalidParameterHandle,
			InvalidInteractionClassHandle,
			FederateNotExecutionMember,
			NotConnected,
			RTIinternalError);

	std::string getFom();

};
PORTICO1516E_NS_END
