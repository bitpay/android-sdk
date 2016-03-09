package com.bitpay.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Policy {

	private String _policy;
	private String _method;
	private List<String> _params;
		
    public Policy() {}

    @JsonIgnore
    public String getPolicy()
    {
    	return _policy;
    }

    @JsonProperty("policy")
    public void setPolicy(String _policy)
    {
    	this._policy = _policy;
    }

    @JsonIgnore
    public String getMethod()
    {
    	return _method;
    }

    @JsonProperty("method")
    public void setMethod(String _method)
    {
    	this._method = _method;
    }

    @JsonIgnore
    public List<String> getParams()
    {
    	return _params;
    }

    @JsonProperty("params")
    public void setParams(List<String> _params)
    {
    	this._params = _params;
    }
}
