#pragma once
#include "RTI/RTIambassador.h"
#include "RTI/portico/IDatatype.h"

namespace rti1516e
{

	class RTI_EXPORT RTIambassadorEx : public virtual RTIambassador {

		//----------------------------------------------------------
		//                   INSTANCE VARIABLES
		//----------------------------------------------------------
	public:
		RTIambassadorEx();
		virtual ~RTIambassadorEx();

		//----------------------------------------------------------
		//                      CONSTRUCTORS
		//----------------------------------------------------------
	public:

		/*ObjectModel getFom();*/

		virtual std::auto_ptr<IDatatype> getAttributeDatatype(ObjectClassHandle whichClass,
		                                 AttributeHandle theHandle)
			throw(AttributeNotDefined,
				InvalidAttributeHandle,
				InvalidObjectClassHandle,
				FederateNotExecutionMember,
				NotConnected,
				RTIinternalError) = 0 ;


		virtual std::auto_ptr<IDatatype> getParameterDatatype(InteractionClassHandle whichClass,
		                                 ParameterHandle theHandle)
			throw (InteractionParameterNotDefined,
				InvalidParameterHandle,
				InvalidInteractionClassHandle,
				FederateNotExecutionMember,
				NotConnected,
				RTIinternalError) = 0;

        virtual std::string getFom() = 0;
	};
}
