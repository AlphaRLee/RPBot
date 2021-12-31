package com.rlee.discordbots.rpbot.profile;

import java.util.HashMap;
import java.util.Map;

/**
 * Attribute with a numeric value.
 * Example: strength with value 5
 * Example: hp with value 20/20
 * @author R Lee
 *
 */
public class NumberAttribute extends Attribute<Integer> {
	private int value;
	private Integer maxValue; //Set to Integer class to allow for no maxValue to be in place
	private Integer minValue;
	
	private Map<Item, Integer> itemEffects; //Effects of particular items relating to this attribute
	
	private int buff;
	private int buffDuration;
	
	private int effectsSum = 0; //Convenience access for sum of all effects. Requires manual updating
	
	public static final int UNLIMITED_BUFF = -1;
	
	public NumberAttribute(String name) {
		super(name);
		setItemEffects(new HashMap<Item, Integer>());
		maxValue = null;
	}

	/**
	 * @return the value of this attribute
	 */
	@Override
	public Integer getValue() {
		return value;
	}

	/**
	 * Set the value of this attribute without bypassing limits
	 * @param value
	 */
	@Override
	public void setValue(Integer value) {
		setValue(value, false);
	}

	/**
	 * @param value the value to set
	 * @param bypassLimits Set to true to ignore limits (eg. maxValue) if set. Set to false to force value 
	 * to limit value if value is outside of limit.
	 */
	public void setValue(int value, boolean bypassLimits) {
		if (!bypassLimits) {
			if (hasMaxValue() && value > maxValue) {
				value = maxValue;
			} else if (hasMinValue() && value < minValue) {
				value = minValue;
			}
		}
		
		this.value = value;
	}

	/**
	 * @return the maxValue
	 */
	public int getMaxValue() {
		return maxValue;
	}

	/**
	 * @param maxValue the maxValue to set. Set to null to disable
	 */
	public void setMaxValue(Integer maxValue) {
		this.maxValue = maxValue;
	}

	public boolean hasMaxValue() {
		return this.maxValue != null;
	}
	
	/**
	 * @return the minValue
	 */
	public int getMinValue() {
		return minValue;
	}

	/**
	 * @param minValue the minValue to set. Set to null to disable
	 */
	public void setMinValue(Integer minValue) {
		this.minValue = minValue;
	}

	public boolean hasMinValue() {
		return this.minValue != null;
	}
	
	/**
	 * @return the itemEffects
	 */
	public Map<Item, Integer> getItemEffects() {
		return itemEffects;
	}
	

	/**
	 * @param itemEffects the itemEffects to set
	 */
	public void setItemEffects(Map<Item, Integer> itemEffects) {
		this.itemEffects = itemEffects;
	}
	
	public Integer getItemEffect(Item item) {
		return itemEffects.get(item);
	}
	
	/**
	 * Get the sum of all item effects on this attribute
	 * @return
	 *
	 * @author R Lee
	 */
	public int getItemEffectsSum() {
//		int sum = 0;
//		
//		for (Integer effect : itemEffects.values()) {
//			sum += (effect != null ? effect : 0);
//		}
//		
//		return sum;
		
		return effectsSum;
	}
	
	/**
	 * Set the effect from an item for this attribute.
	 * Increments effectsSum by effect amount
	 * @param item Item that effect applies to. Will do nothing if item is null
	 * @param effect
	 *
	 * @author R Lee
	 */
	public void setItemEffect(Item item, Integer effect) {
		if (item == null) {
			return;
		}
		
		itemEffects.put(item, effect);
		effectsSum += effect;
	}
	
	/**
	 * Remove the effect of a particular item from this attribute.
	 * Decrements effectsSum by the item's effect amount
	 * @param item
	 *
	 * @author R Lee
	 */
	public void removeItemEffect(Item item) {
		if (item == null) {
			return;
		}
		
		Integer effect = itemEffects.remove(item);
		if (effect != null) {
			effectsSum -= effect;
		}
	}
	
	/**
	 * Determine whether or not any item effects are active on this attribute
	 * @return True if item effects are present
	 *
	 * @author R Lee
	 */
	public boolean hasItemEffects() {
		return !itemEffects.isEmpty();
	}
	

	/**
	 * @return the buff value, defaults to 0
	 */
	public int getBuff() {
		return buff;
	}

	/**
	 * @param buff the buff to set. Set to 0 to disable (sets duration to 0 as well)
	 */
	public void setBuff(int buff, int duration) {
		this.buff = buff;
		setBuffDuration(buff != 0 ? duration : 0);
	}
	
	/**
	 * @param buff
	 * @param isUnlimited Set to true to give unlimited duration bonus. Set to false to set duration to 1
	 *
	 * @author R Lee
	 */
	public void setBuff(int buff, boolean isUnlimited) {
		setBuff(buff, isUnlimited ? UNLIMITED_BUFF : 1);
	}
	
	public boolean hasBuff() {
		return buff != 0;
	}
	
	/**
	 * @return the buffDuration
	 */
	public int getBuffDuration() {
		return buffDuration;
	}

	/**
	 * @param buffDuration Length of buff
	 */
	public void setBuffDuration(int buffDuration) {
		this.buffDuration = buffDuration;
	}
	
	/**
	 * Decrement the duration of the buff if buff duration is a value greater than 0
	 * @param resetBuffAtZero If true and buff duration decrements to 0, buff is set to 0
	 * @author R Lee
	 */
	public void decrementBuffDuration(boolean resetBuffAtZero) {
		if (buffDuration > 0) {
			buffDuration--;
			
			if (resetBuffAtZero && buffDuration == 0) {
				buff = 0;
			}
		} 
	}
	
	public void addToValue(int x, boolean bypassLimits) {
		int currentValue = getValue();
		int finalX = x; //Final value that x will be computed to when setValue() is called
		boolean finalBypassLimits = bypassLimits; //Final value that bypassLimits will take when setValue() is called
		
		//Modify x and bypassLimits values based on initial conditions (and bypassLimits is already false)
		if (!bypassLimits) {
    		if (x > 0) {
    			if (hasMaxValue()) {
    				if (currentValue >= maxValue) {
    					//Do not chop off current value bonus that is already out of bounds, just grant no more
    					finalX = 0;
    					finalBypassLimits = true;
    				} else if (currentValue + x <= maxValue) {
    					//Final result will be within bounds
        				finalBypassLimits = true;
        			}
    			} else {
    				finalBypassLimits = true;
    			}
    		} else if (x < 0) {
    			if (hasMinValue()) {
    				if (currentValue <= minValue) {
    					//Do not chop off current value bonus that is already out of bounds, just take no more
    					finalX = 0;
    					finalBypassLimits = true;
    				} else if (currentValue + x >= minValue) {
    					//Final results will be within bounds
        				finalBypassLimits = true;
        			}
    			} else {
    				finalBypassLimits = true;
    			}
    		}
		}
		
		setValue(currentValue + finalX, finalBypassLimits);
	}
}
