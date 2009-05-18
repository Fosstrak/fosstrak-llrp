/*
 * Copyright (C) 2007 ETH Zurich
 *
 * This file is part of Fosstrak (www.fosstrak.org).
 *
 * Fosstrak is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software Foundation.
 *
 * Fosstrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Fosstrak; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package org.fosstrak.llrp.commander.editors;

import java.util.*;

import org.apache.log4j.Logger;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.contentassist.*;

/**
* ...
* @author zhanghao
*
*/
public class LLRPContentAssistant implements IContentAssistProcessor {
	
	/**
	 * Log4j instance.
	 */
	private static Logger log = Logger.getLogger(LLRPContentAssistant.class);
	
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int documentOffset) {
		ICompletionProposal[] proposals = null;
		try {
			IDocument document = viewer.getDocument();
			IRegion range = document.getLineInformationOfOffset(documentOffset);
			int start = range.getOffset();
			String prefix = document.get(start, documentOffset - start);
			
			ConfigurationModel model = new ConfigurationModel(document.get());
			List completions = model.getCompletions(prefix);

			proposals = new CompletionProposal[completions.size()];
			int i = 0;
			for (Iterator iter = completions.iterator(); iter.hasNext();) {
				String completion = (String) iter.next();
				proposals[i++] = new CompletionProposal(completion, start,
						documentOffset - start, completion.length());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return proposals;
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int documentOffset) {
		return null;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { ':' };
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	public String getErrorMessage() {
		return "No completions available.";
	}

	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}
}
