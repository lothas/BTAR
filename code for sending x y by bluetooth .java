   private byte[] Updating_x_y_for_sending(double val) {
    	byte[] message = new byte[3]; 
    	if (val >= 0){
    		val = ( Math.min( 10*val, 100.0 ) );
    		message[0] = 50;
    	}else{
    		message[0] = 49;
    		val = 100 + ( Math.max( 10*val, -100.0 ) );
    	}
    	message[1] =(byte)((int)(val/10)+48);
    	message[2] = (byte)((val % 10)+48);
    	
    	return message;
    }
    
    private void UpdateMethod() {
    	
    	// if either of the joysticks is not on the center, or timeout occurred
    	if(!mCenterL || !mCenterR || (mTimeoutCounter>=mMaxTimeoutCount && mMaxTimeoutCount>-1) ) {
    		// limit to {0..100}
    		
    		byte[] x_message = new byte[3];
	    	byte[] y_message = new byte[3];
    		
    		x_message = Updating_x_y_for_sending(x_axeL);
	    	y_message = Updating_x_y_for_sending(y_axeL);
    		
	    	byte radiusL = (byte) ( Math.min( mRadiusL, 10.0 ) );
	    	byte radiusR = (byte) ( Math.min( mRadiusR, 10.0 ) );
    		// scale to {0..35}
	    	byte angleL = (byte) ( mAngleL * 18.0 / Math.PI + 36.0 + 0.5 );
	    	byte angleR = (byte) ( mAngleR * 18.0 / Math.PI + 36.0 + 0.5 );
	    	if( angleL >= 36 )	angleL = (byte)(angleL-36);
	    	if( angleR >= 36 )	angleR = (byte)(angleR-36);
	    	
	    	if (D) {
	    		Log.d(TAG, String.format("%d, %d, %d, %d", radiusL, angleL, radiusR, angleR ) );
	    	}
	    	
	    	if( mDataFormat==4 ) {
	    		// raw 4 bytes
	    		sendMessage( new String(new byte[] {
		    			radiusL, angleL, radiusR, angleR } ) );
	    	}else if( mDataFormat==5 ) {
	    		// start with 0x55
		    	sendMessage( new String(new byte[] {
		    			0x55, radiusL, angleL, radiusR, angleR } ) );
	    	}else if( mDataFormat==6 ) {
	    		// use STX & ETX
		    	sendMessage( new String(new byte[] {
		    			0x02, x_message[0], x_message[1], x_message[2], y_message[0], y_message[1], x_message[2], 0x03 } ) );
	    	}
	    	
	    	mTimeoutCounter = 0;
    	}
    	else{
    		if( mMaxTimeoutCount>-1 )
    			mTimeoutCounter++;
    	}	
    }