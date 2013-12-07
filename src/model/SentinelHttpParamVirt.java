/*
 * Copyright (C) 2013 DobinRutishauser@broken.ch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package model;

import burp.IParameter;
import util.BurpCallbacks;

/**
 *
 * @author DobinRutishauser@broken.ch
 */
public class SentinelHttpParamVirt extends SentinelHttpParam {
    private SentinelHttpParam parentParam = null; // For reference purposes only
    
    public enum EncoderType {
        Base64,
        URL,
        HTML,
    }
    
    private EncoderType encoderType;
    
    public SentinelHttpParamVirt(SentinelHttpParam parentParam, EncoderType type) {
        super(parentParam);
        
        this.encoderType = type;
        this.parentParam = parentParam;
    }
    
    public SentinelHttpParamVirt(SentinelHttpParamVirt virt) {
        super(virt);
        this.encoderType = virt.encoderType;
        this.parentParam = virt.parentParam;
    }
    
    @Override
    public String getDecodedValue() {
        String mutatedValue = "";
        
        switch (encoderType) {
            case Base64:
                byte[] mutated = BurpCallbacks.getInstance().getBurp().getHelpers().base64Decode(value);
                mutatedValue = "BASE64: " + new String(mutated);
                break;
            default:
                mutatedValue = value;
        }

        return mutatedValue;
    }
    
    @Override
    public void changeValue(String v) {
        String mutatedValue;
        
        switch (encoderType) {
            case Base64:
                byte[] b = BurpCallbacks.getInstance().getBurp().getHelpers().stringToBytes(v);
                mutatedValue = BurpCallbacks.getInstance().getBurp().getHelpers().base64Encode(b);
                break;
            default:
                mutatedValue = value;
        }

        this.value = mutatedValue;
        this.valueEnd = this.valueStart + value.length();
    }
    
}
