/*
 *     Copyright 2021 Horstexplorer @ https://www.netbeacon.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.netbeacon.d43z.one.objects.base;

import de.netbeacon.d43z.one.objects.bp.IMatchable;
import de.netbeacon.d43z.one.objects.bp.ISimilarity;
import de.netbeacon.d43z.one.objects.eval.ContentMatch;

import java.util.List;

public class ContentShard implements IMatchable{

	private final ContentContext parent;
	private final List<Content> orderedContent;

	public ContentShard(ContentContext parent, List<Content> orderedContent){
		this.parent = parent;
		this.orderedContent = orderedContent;
	}

	public ContentContext getParent(){
		return parent;
	}

	public List<Content> getOrderedContent(){
		return orderedContent;
	}

	@Override
	public ContentMatch getMatchFor(ISimilarity.Algorithm algorithm, Content content){
		float bestF = 0;
		Content bestI = null;
		Content bestO = null;
		for(int i = 0; i < orderedContent.size() - 1; i++){
			float current = orderedContent.get(i).eval(algorithm, content);
			if(bestF < current){
				bestF = current;
				bestI = orderedContent.get(i);
				bestO = orderedContent.get(i + 1);
			}
		}
		return new ContentMatch(content, bestI, bestO, this, bestF);
	}

}
