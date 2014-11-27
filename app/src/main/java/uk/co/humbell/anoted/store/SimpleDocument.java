/*
 * Copyright (c) 2014 Nathaniel Bennett.
 *
 * This file is part of Anoted android application project.
 *
 * Anoted is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Anoted is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Anoted.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.co.humbell.anoted.store;

/**
 * Created by nathan on 19/11/14.
 */
public interface SimpleDocument {

    /**
     * @return the ID of the document
     */
    public Long getID();

    /**
     * @return the name of the document
     */
    public String getName();

    /**
     * @return the content of the document
     */
    public String getContent();

    /**
     * @return true if representation has an Id
     */
    public boolean hasID();

    /**
     * @return true if representation contains a name
     */
    public boolean hasName();

    /**
     * @return returns true if representation contains document contents
     */
    public boolean hasContent();
}
