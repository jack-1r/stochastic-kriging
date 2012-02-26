package se.sics.kriging.components;

import se.sics.kompics.PortType;

public class PortApplication extends PortType {{
    
	request(RequestExpStart.class);
	indication(NotifyExecCompleted.class);
	request(RequestSK.class);
	indication(NotifySKCompleted.class);
	request(RequestMSEMinimization.class);
	indication(NotifyMinimizationStepCompleted.class);
	request(RequestMinimizationContinue.class);
	request(RequestOptimal.class);
	indication(NotifyMinimizationCompleted.class);
	indication(NotifyExpResult.class);
	request(RequestGraphDrawing.class);
	request(RequestAbortion.class);
}}
