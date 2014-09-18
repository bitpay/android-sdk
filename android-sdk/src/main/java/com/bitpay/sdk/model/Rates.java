package com.bitpay.sdk.model;

import java.util.List;

/**
 * Hold information about rates for multiple currencies.
 */
public class Rates {

    private List<Rate> _rates;

    public Rates(List<Rate> rates) {
        _rates = rates;
    }

    public List<Rate> getRates()
    {
	    return _rates;
    }

    public void update(List<Rate> rates) {
	    _rates = rates;
    }

    public double getRate(String currencyCode) {
	    double val = 0;
	    for (Rate rateObj : _rates)
        {
		    if (rateObj.getCode().equals(currencyCode))
            {
                val = rateObj.getValue();
                break;
		    }
	    }
		return val;
    }
}
