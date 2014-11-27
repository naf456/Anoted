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
 * This file was created by nathan on 20/11/14.
 */

import java.util.List;

/**
 * This file was created by nathan on 21/11/14.
 */

public interface DocumentStore<D> {


    /**
     * runs any needed storage initialization routine
     */
    public void open();

    /**
     * runs any needed storage shutdown routine
     */
    public void close();

    /**
     * Create  a new document entity in the store
     * @param name the name you want to give to the created document
     * @return representation of the newly created document
     * @throws DocumentCreateException
     */
    public D createDocument(String name) throws DocumentCreateException;

    /**
     * Returns a document from the store by using an id
     * @param id the Id of the document to retrieve
     * @param requestedProperties the properties of the object you want to retrieve
     * @return the document found in the store
     * @throws DocumentRetrieveException
     */
    public D getDocumentById(Long id, int[] requestedProperties) throws DocumentRetrieveException;

    /**
     * Retrieves a page of documents from an offset.
     * @param width the width of the page
     * @param offset the offset of the page from document 0
     * @param requestedProperties the properties to retrieve of the documents
     * @return  a list of the documents in the page
     */
    public List<D> getPageOfDocuments(int width, int offset, int[] requestedProperties) throws DocumentRetrieveException;

    /**
     * Syncs the document to the store
     * @param document the document to be synced
     * @throws uk.co.humbell.anoted.store.DocumentStore.DocumentSyncException
     */
    public void syncDocument(D document) throws DocumentSyncException;

    /**
     * Removes a document from the database
     * @param document
     * @throws DocumentDeleteException
     */
    public void deleteDocument(D document) throws DocumentDeleteException;

    /**
     * @param observer the {@link DocumentStoreObserver} to receive store updates
     */
    public void registerDocumentStoreObserver(DocumentStoreObserver observer);

    /**
     * @param observer the {@link DocumentStoreObserver} which you want the store updates to stop
     */
    public void unregisterDocumentStoreObserver(DocumentStoreObserver observer);

    /**
     * notifies all store observers of a potential change in the document store
     */
    public void notifyDocumentStoreChange();

    public interface DocumentStoreObserver {
        /**
         * used as a callback when a document state changes within the document store
         */
        public void onDocumentStoreChange();
    }


    //TODO: provide methods which allow more detailed exceptions
    public class DocumentCreateException extends RuntimeException {}

    public class DocumentRetrieveException extends RuntimeException {}

    public class DocumentSyncException extends RuntimeException {}

    public class DocumentDeleteException extends RuntimeException {}
}
