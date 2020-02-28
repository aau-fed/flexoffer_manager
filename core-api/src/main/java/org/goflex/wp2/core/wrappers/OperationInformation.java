package org.goflex.wp2.core.wrappers;

/*-
 * #%L
 * ARROWHEAD::WP5::Core Data Structures
 * %%
 * Copyright (C) 2016 The ARROWHEAD Consortium
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import org.goflex.wp2.core.entities.ConsumptionTuple;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * TODO: TempWrapper for INEA
 * @author Bijay
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class OperationInformation implements Serializable {



    private static final long serialVersionUID = 7959016934179550864L;

    private int priority = 1;

	private int operationState = 1;

	private int intervalLength = 900;

	private  ConsumptionTuple[] accountingPower;

	private  ConsumptionTuple[] operationPower;

	private ConsumptionTuple[] operationPrognosis;

	public OperationInformation() {}

	public OperationInformation(int priority, int operationState, int intervalLength, ConsumptionTuple[] accountingPower, ConsumptionTuple[] operationPower, ConsumptionTuple[] operationPrognosis) {
		this.priority = priority;
		this.operationState = operationState;
		this.intervalLength = intervalLength;
		this.accountingPower = accountingPower;
		this.operationPower = operationPower;
		this.operationPrognosis = operationPrognosis;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getIntervalLength() {
		return intervalLength;
	}

	public void setIntervalLength(int intervalLength) {
		this.intervalLength = intervalLength;
	}

	public ConsumptionTuple[] getAccountingPower() {
		return accountingPower;
	}

	public void setAccountingPower(ConsumptionTuple[] accountingPower) {
		this.accountingPower = accountingPower;
	}

	public int getOperationState() {
		return operationState;
	}

	public void setOperationState(int operationState) {
		this.operationState = operationState;
	}



	public ConsumptionTuple[] getOperationPower() {
		return operationPower;
	}

	public void setOperationPower(ConsumptionTuple[] operationPower) {
		this.operationPower = operationPower;
	}

	public ConsumptionTuple[] getOperationPrognosis() {
		return operationPrognosis;
	}

	public void setOperationPrognosis(ConsumptionTuple[] operationPrognosis) {
		this.operationPrognosis = operationPrognosis;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		OperationInformation that = (OperationInformation) o;
		return priority == that.priority &&
				operationState == that.operationState &&
				intervalLength == that.intervalLength &&
				Arrays.equals(accountingPower, that.accountingPower) &&
				Arrays.equals(operationPower, that.operationPower) &&
				Arrays.equals(operationPrognosis, that.operationPrognosis);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(priority, operationState, intervalLength);
		result = 31 * result + Arrays.hashCode(accountingPower);
		result = 31 * result + Arrays.hashCode(operationPower);
		result = 31 * result + Arrays.hashCode(operationPrognosis);
		return result;
	}

	@Override
	public String toString() {
		return "OperationInformation{" +
				"priority=" + priority +
				", operationState=" + operationState +
				", intervalLength=" + intervalLength +
				", accountingPower=" + Arrays.toString(accountingPower) +
				", operationPower=" + Arrays.toString(operationPower) +
				", operationPrognosis=" + Arrays.toString(operationPrognosis) +
				'}';
	}

}
