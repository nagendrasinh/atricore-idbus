/*
 * Copyright (c) 2009., Atricore Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.atricore.idbus.console.lifecycle.main.spi;

import com.atricore.idbus.console.lifecycle.main.domain.metadata.*;

/**
 * @author <a href="mailto:sgonzalez@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id$
 */
public interface IdentityApplianceDefinitionVisitor {

    /**
     * This method is called before walking any children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to perform any initialization tasks it needs for walking the node's
     * children.
     *
     * @param node the node to be walked
     */
    void arrive(IdentityApplianceDefinition node) throws Exception;

    /**
     * This method is called after walking the children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to compute the result of walking the argument <code>node</code>
     * and it's children. This result is returned by the tree walker's
     * <code>walk</code> method. The argument <code>results</code> holds
     * the results of walking the children of the argument <code>node</code>.
     * Usually, the result of the argument <code>node</code> is computed in
     * consideration of the results of its' children.
     *
     * @param node the node having been walked
     * @param results the results of walking the node's children
     * @return the result of walking the node and it's children
     */
    Object[] leave(IdentityApplianceDefinition node, Object[] results) throws Exception;

    /**
     * This method is called before walking each child of the argument
     * <code>node</code>. The return value of this method determines if
     * the next child of the argument <code>node</code> should be walked.
     *  The argument <code>resultOfPreviousChild</code>
     * holds the result of walking the previous child of the argument
     * <code>node</code>. Usually, it is used to determine the return value
     * of this method. The argument <code>indexOfNextChild</code>
     * determines the index of the next child to be walked. This index
     * determines the position in the children array of the argument <code>node</code>.
     * Note: The index of the first child is 0.
     *
     * @param node                  the parent node of the children currently walked
     * @param resultOfPreviousChild the result of walking the node's previous child
     * @param indexOfNextChild      the index of the next child to be walked
     * @return <code>false</code>, if no more childs should be walked, else <code>true</code>
     */
    boolean walkNextChild(IdentityApplianceDefinition node, Object child, Object resultOfPreviousChild, int indexOfNextChild);


    /**
     * This method is called before walking any children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to perform any initialization tasks it needs for walking the node's
     * children.
     *
     * @param node the node to be walked
     */
    void arrive(ServiceProvider node) throws Exception;

    /**
     * This method is called after walking the children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to compute the result of walking the argument <code>node</code>
     * and it's children. This result is returned by the tree walker's
     * <code>walk</code> method. The argument <code>results</code> holds
     * the results of walking the children of the argument <code>node</code>.
     * Usually, the result of the argument <code>node</code> is computed in
     * consideration of the results of its' children.
     *
     * @param node the node having been walked
     * @param results the results of walking the node's children
     * @return the result of walking the node and it's children
     */
    Object[] leave(ServiceProvider node, Object[] results) throws Exception;

    /**
     * This method is called before walking each child of the argument
     * <code>node</code>. The return value of this method determines if
     * the next child of the argument <code>node</code> should be walked.
     *  The argument <code>resultOfPreviousChild</code>
     * holds the result of walking the previous child of the argument
     * <code>node</code>. Usually, it is used to determine the return value
     * of this method. The argument <code>indexOfNextChild</code>
     * determines the index of the next child to be walked. This index
     * determines the position in the children array of the argument <code>node</code>.
     * Note: The index of the first child is 0.
     *
     * @param node                  the parent node of the children currently walked
     * @param resultOfPreviousChild the result of walking the node's previous child
     * @param indexOfNextChild      the index of the next child to be walked
     * @return <code>false</code>, if no more childs should be walked, else <code>true</code>
     */
    boolean walkNextChild(ServiceProvider node, Object child, Object resultOfPreviousChild, int indexOfNextChild);


    /**
     * This method is called before walking any children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to perform any initialization tasks it needs for walking the node's
     * children.
     *
     * @param node the node to be walked
     */
    void arrive(IdentityProvider node) throws Exception;

    /**
     * This method is called after walking the children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to compute the result of walking the argument <code>node</code>
     * and it's children. This result is returned by the tree walker's
     * <code>walk</code> method. The argument <code>results</code> holds
     * the results of walking the children of the argument <code>node</code>.
     * Usually, the result of the argument <code>node</code> is computed in
     * consideration of the results of its' children.
     *
     * @param node the node having been walked
     * @param results the results of walking the node's children
     * @return the result of walking the node and it's children
     */
    Object[] leave(IdentityProvider node, Object[] results) throws Exception;

    /**
     * This method is called before walking each child of the argument
     * <code>node</code>. The return value of this method determines if
     * the next child of the argument <code>node</code> should be walked.
     *  The argument <code>resultOfPreviousChild</code>
     * holds the result of walking the previous child of the argument
     * <code>node</code>. Usually, it is used to determine the return value
     * of this method. The argument <code>indexOfNextChild</code>
     * determines the index of the next child to be walked. This index
     * determines the position in the children array of the argument <code>node</code>.
     * Note: The index of the first child is 0.
     *
     * @param node                  the parent node of the children currently walked
     * @param resultOfPreviousChild the result of walking the node's previous child
     * @param indexOfNextChild      the index of the next child to be walked
     * @return <code>false</code>, if no more childs should be walked, else <code>true</code>
     */
    boolean walkNextChild(IdentityProvider node, Object child, Object resultOfPreviousChild, int indexOfNextChild);
    
    
    /**
     * This method is called before walking any children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to perform any initialization tasks it needs for walking the node's
     * children.
     *
     * @param node the node to be walked
     */
    void arrive(IdentityProviderChannel node) throws Exception;

    /**
     * This method is called after walking the children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to compute the result of walking the argument <code>node</code>
     * and it's children. This result is returned by the tree walker's
     * <code>walk</code> method. The argument <code>results</code> holds
     * the results of walking the children of the argument <code>node</code>.
     * Usually, the result of the argument <code>node</code> is computed in
     * consideration of the results of its' children.
     *
     * @param node the node having been walked
     * @param results the results of walking the node's children
     * @return the result of walking the node and it's children
     */
    Object[] leave(IdentityProviderChannel node, Object[] results) throws Exception;

    /**
     * This method is called before walking each child of the argument
     * <code>node</code>. The return value of this method determines if
     * the next child of the argument <code>node</code> should be walked.
     *  The argument <code>resultOfPreviousChild</code>
     * holds the result of walking the previous child of the argument
     * <code>node</code>. Usually, it is used to determine the return value
     * of this method. The argument <code>indexOfNextChild</code>
     * determines the index of the next child to be walked. This index
     * determines the position in the children array of the argument <code>node</code>.
     * Note: The index of the first child is 0.
     *
     * @param node                  the parent node of the children currently walked
     * @param resultOfPreviousChild the result of walking the node's previous child
     * @param indexOfNextChild      the index of the next child to be walked
     * @return <code>false</code>, if no more childs should be walked, else <code>true</code>
     */
    boolean walkNextChild(IdentityProviderChannel node, Object child, Object resultOfPreviousChild, int indexOfNextChild);
    
    
    /**
     * This method is called before walking any children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to perform any initialization tasks it needs for walking the node's
     * children.
     *
     * @param node the node to be walked
     */
    void arrive(ServiceProviderChannel node) throws Exception;

    /**
     * This method is called after walking the children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to compute the result of walking the argument <code>node</code>
     * and it's children. This result is returned by the tree walker's
     * <code>walk</code> method. The argument <code>results</code> holds
     * the results of walking the children of the argument <code>node</code>.
     * Usually, the result of the argument <code>node</code> is computed in
     * consideration of the results of its' children.
     *
     * @param node the node having been walked
     * @param results the results of walking the node's children
     * @return the result of walking the node and it's children
     */
    Object[] leave(ServiceProviderChannel node, Object[] results) throws Exception;

    /**
     * This method is called before walking each child of the argument
     * <code>node</code>. The return value of this method determines if
     * the next child of the argument <code>node</code> should be walked.
     *  The argument <code>resultOfPreviousChild</code>
     * holds the result of walking the previous child of the argument
     * <code>node</code>. Usually, it is used to determine the return value
     * of this method. The argument <code>indexOfNextChild</code>
     * determines the index of the next child to be walked. This index
     * determines the position in the children array of the argument <code>node</code>.
     * Note: The index of the first child is 0.
     *
     * @param node                  the parent node of the children currently walked
     * @param resultOfPreviousChild the result of walking the node's previous child
     * @param indexOfNextChild      the index of the next child to be walked
     * @return <code>false</code>, if no more childs should be walked, else <code>true</code>
     */
    boolean walkNextChild(ServiceProviderChannel node, Object child, Object resultOfPreviousChild, int indexOfNextChild);

    /**
     * This method is called before walking any children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to perform any initialization tasks it needs for walking the node's
     * children.
     *
     * @param node the node to be walked
     */
    void arrive(IdentitySource node) throws Exception;

    /**
     * This method is called after walking the children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to compute the result of walking the argument <code>node</code>
     * and it's children. This result is returned by the tree walker's
     * <code>walk</code> method. The argument <code>results</code> holds
     * the results of walking the children of the argument <code>node</code>.
     * Usually, the result of the argument <code>node</code> is computed in
     * consideration of the results of its' children.
     *
     * @param node the node having been walked
     * @param results the results of walking the node's children
     * @return the result of walking the node and it's children
     */
    Object[] leave(IdentitySource node, Object[] results) throws Exception;

    /**
     * This method is called before walking each child of the argument
     * <code>node</code>. The return value of this method determines if
     * the next child of the argument <code>node</code> should be walked.
     *  The argument <code>resultOfPreviousChild</code>
     * holds the result of walking the previous child of the argument
     * <code>node</code>. Usually, it is used to determine the return value
     * of this method. The argument <code>indexOfNextChild</code>
     * determines the index of the next child to be walked. This index
     * determines the position in the children array of the argument <code>node</code>.
     * Note: The index of the first child is 0.
     *
     * @param node                  the parent node of the children currently walked
     * @param resultOfPreviousChild the result of walking the node's previous child
     * @param indexOfNextChild      the index of the next child to be walked
     * @return <code>false</code>, if no more childs should be walked, else <code>true</code>
     */
    boolean walkNextChild(IdentitySource node, Object child, Object resultOfPreviousChild, int indexOfNextChild);    

    /**
     * This method is called before walking any children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to perform any initialization tasks it needs for walking the node's
     * children.
     *
     * @param node the node to be walked
     */
    void arrive(EmbeddedIdentitySource node) throws Exception;

    /**
     * This method is called after walking the children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to compute the result of walking the argument <code>node</code>
     * and it's children. This result is returned by the tree walker's
     * <code>walk</code> method. The argument <code>results</code> holds
     * the results of walking the children of the argument <code>node</code>.
     * Usually, the result of the argument <code>node</code> is computed in
     * consideration of the results of its' children.
     *
     * @param node the node having been walked
     * @param results the results of walking the node's children
     * @return the result of walking the node and it's children
     */
    Object[] leave(EmbeddedIdentitySource node, Object[] results) throws Exception;

    /**
     * This method is called before walking each child of the argument
     * <code>node</code>. The return value of this method determines if
     * the next child of the argument <code>node</code> should be walked.
     *  The argument <code>resultOfPreviousChild</code>
     * holds the result of walking the previous child of the argument
     * <code>node</code>. Usually, it is used to determine the return value
     * of this method. The argument <code>indexOfNextChild</code>
     * determines the index of the next child to be walked. This index
     * determines the position in the children array of the argument <code>node</code>.
     * Note: The index of the first child is 0.
     *
     * @param node                  the parent node of the children currently walked
     * @param resultOfPreviousChild the result of walking the node's previous child
     * @param indexOfNextChild      the index of the next child to be walked
     * @return <code>false</code>, if no more childs should be walked, else <code>true</code>
     */
    boolean walkNextChild(EmbeddedIdentitySource node, Object child, Object resultOfPreviousChild, int indexOfNextChild);

    /**
     * This method is called before walking any children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to perform any initialization tasks it needs for walking the node's
     * children.
     *
     * @param node the node to be walked
     */
    void arrive(LdapIdentitySource node) throws Exception;

    /**
     * This method is called after walking the children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to compute the result of walking the argument <code>node</code>
     * and it's children. This result is returned by the tree walker's
     * <code>walk</code> method. The argument <code>results</code> holds
     * the results of walking the children of the argument <code>node</code>.
     * Usually, the result of the argument <code>node</code> is computed in
     * consideration of the results of its' children.
     *
     * @param node the node having been walked
     * @param results the results of walking the node's children
     * @return the result of walking the node and it's children
     */
    Object[] leave(LdapIdentitySource node, Object[] results) throws Exception;

    /**
     * This method is called before walking each child of the argument
     * <code>node</code>. The return value of this method determines if
     * the next child of the argument <code>node</code> should be walked.
     *  The argument <code>resultOfPreviousChild</code>
     * holds the result of walking the previous child of the argument
     * <code>node</code>. Usually, it is used to determine the return value
     * of this method. The argument <code>indexOfNextChild</code>
     * determines the index of the next child to be walked. This index
     * determines the position in the children array of the argument <code>node</code>.
     * Note: The index of the first child is 0.
     *
     * @param node                  the parent node of the children currently walked
     * @param resultOfPreviousChild the result of walking the node's previous child
     * @param indexOfNextChild      the index of the next child to be walked
     * @return <code>false</code>, if no more childs should be walked, else <code>true</code>
     */
    boolean walkNextChild(LdapIdentitySource node, Object child, Object resultOfPreviousChild, int indexOfNextChild);

    /**
     * This method is called before walking any children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to perform any initialization tasks it needs for walking the node's
     * children.
     *
     * @param node the node to be walked
     */
    void arrive(DbIdentitySource node) throws Exception;

    /**
     * This method is called after walking the children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to compute the result of walking the argument <code>node</code>
     * and it's children. This result is returned by the tree walker's
     * <code>walk</code> method. The argument <code>results</code> holds
     * the results of walking the children of the argument <code>node</code>.
     * Usually, the result of the argument <code>node</code> is computed in
     * consideration of the results of its' children.
     *
     * @param node the node having been walked
     * @param results the results of walking the node's children
     * @return the result of walking the node and it's children
     */
    Object[] leave(DbIdentitySource node, Object[] results) throws Exception;

    /**
     * This method is called before walking each child of the argument
     * <code>node</code>. The return value of this method determines if
     * the next child of the argument <code>node</code> should be walked.
     *  The argument <code>resultOfPreviousChild</code>
     * holds the result of walking the previous child of the argument
     * <code>node</code>. Usually, it is used to determine the return value
     * of this method. The argument <code>indexOfNextChild</code>
     * determines the index of the next child to be walked. This index
     * determines the position in the children array of the argument <code>node</code>.
     * Note: The index of the first child is 0.
     *
     * @param node                  the parent node of the children currently walked
     * @param resultOfPreviousChild the result of walking the node's previous child
     * @param indexOfNextChild      the index of the next child to be walked
     * @return <code>false</code>, if no more childs should be walked, else <code>true</code>
     */
    boolean walkNextChild(DbIdentitySource node, Object child, Object resultOfPreviousChild, int indexOfNextChild);

    /**
     * This method is called before walking any children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to perform any initialization tasks it needs for walking the node's
     * children.
     *
     * @param node the node to be walked
     */
    void arrive(JOSSOActivation node) throws Exception;

    /**
     * This method is called after walking the children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to compute the result of walking the argument <code>node</code>
     * and it's children. This result is returned by the tree walker's
     * <code>walk</code> method. The argument <code>results</code> holds
     * the results of walking the children of the argument <code>node</code>.
     * Usually, the result of the argument <code>node</code> is computed in
     * consideration of the results of its' children.
     *
     * @param node the node having been walked
     * @param results the results of walking the node's children
     * @return the result of walking the node and it's children
     */
    Object[] leave(JOSSOActivation node, Object[] results) throws Exception;

    /**
     * This method is called before walking each child of the argument
     * <code>node</code>. The return value of this method determines if
     * the next child of the argument <code>node</code> should be walked.
     *  The argument <code>resultOfPreviousChild</code>
     * holds the result of walking the previous child of the argument
     * <code>node</code>. Usually, it is used to determine the return value
     * of this method. The argument <code>indexOfNextChild</code>
     * determines the index of the next child to be walked. This index
     * determines the position in the children array of the argument <code>node</code>.
     * Note: The index of the first child is 0.
     *
     * @param node                  the parent node of the children currently walked
     * @param resultOfPreviousChild the result of walking the node's previous child
     * @param indexOfNextChild      the index of the next child to be walked
     * @return <code>false</code>, if no more childs should be walked, else <code>true</code>
     */
    boolean walkNextChild(JOSSOActivation node, Object child, Object resultOfPreviousChild, int indexOfNextChild);

    /**
     * This method is called before walking any children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to perform any initialization tasks it needs for walking the node's
     * children.
     *
     * @param node the node to be walked
     */
    void arrive(Location node) throws Exception;

    /**
     * This method is called after walking the children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to compute the result of walking the argument <code>node</code>
     * and it's children. This result is returned by the tree walker's
     * <code>walk</code> method. The argument <code>results</code> holds
     * the results of walking the children of the argument <code>node</code>.
     * Usually, the result of the argument <code>node</code> is computed in
     * consideration of the results of its' children.
     *
     * @param node the node having been walked
     * @param results the results of walking the node's children
     * @return the result of walking the node and it's children
     */
    Object[] leave(Location node, Object[] results) throws Exception;

    /**
     * This method is called before walking each child of the argument
     * <code>node</code>. The return value of this method determines if
     * the next child of the argument <code>node</code> should be walked.
     *  The argument <code>resultOfPreviousChild</code>
     * holds the result of walking the previous child of the argument
     * <code>node</code>. Usually, it is used to determine the return value
     * of this method. The argument <code>indexOfNextChild</code>
     * determines the index of the next child to be walked. This index
     * determines the position in the children array of the argument <code>node</code>.
     * Note: The index of the first child is 0.
     *
     * @param node                  the parent node of the children currently walked
     * @param resultOfPreviousChild the result of walking the node's previous child
     * @param indexOfNextChild      the index of the next child to be walked
     * @return <code>false</code>, if no more childs should be walked, else <code>true</code>
     */
    boolean walkNextChild(Location node, Object child, Object resultOfPreviousChild, int indexOfNextChild);

    /**
     * This method is called before walking any children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to perform any initialization tasks it needs for walking the node's
     * children.
     *
     * @param node the node to be walked
     */
    void arrive(FederatedConnection node) throws Exception;

    /**
     * This method is called after walking the children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to compute the result of walking the argument <code>node</code>
     * and it's children. This result is returned by the tree walker's
     * <code>walk</code> method. The argument <code>results</code> holds
     * the results of walking the children of the argument <code>node</code>.
     * Usually, the result of the argument <code>node</code> is computed in
     * consideration of the results of its' children.
     *
     * @param node the node having been walked
     * @param results the results of walking the node's children
     * @return the result of walking the node and it's children
     */
    Object[] leave(FederatedConnection node, Object[] results) throws Exception;

    /**
     * This method is called before walking each child of the argument
     * <code>node</code>. The return value of this method determines if
     * the next child of the argument <code>node</code> should be walked.
     *  The argument <code>resultOfPreviousChild</code>
     * holds the result of walking the previous child of the argument
     * <code>node</code>. Usually, it is used to determine the return value
     * of this method. The argument <code>indexOfNextChild</code>
     * determines the index of the next child to be walked. This index
     * determines the position in the children array of the argument <code>node</code>.
     * Note: The index of the first child is 0.
     *
     * @param node                  the parent node of the children currently walked
     * @param resultOfPreviousChild the result of walking the node's previous child
     * @param indexOfNextChild      the index of the next child to be walked
     * @return <code>false</code>, if no more childs should be walked, else <code>true</code>
     */
    boolean walkNextChild(FederatedConnection node, Object child, Object resultOfPreviousChild, int indexOfNextChild);
    
    /**
     * This method is called before walking any children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to perform any initialization tasks it needs for walking the node's
     * children.
     *
     * @param node the node to be walked
     */
    void arrive(IdentityLookup node) throws Exception;

    /**
     * This method is called after walking the children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to compute the result of walking the argument <code>node</code>
     * and it's children. This result is returned by the tree walker's
     * <code>walk</code> method. The argument <code>results</code> holds
     * the results of walking the children of the argument <code>node</code>.
     * Usually, the result of the argument <code>node</code> is computed in
     * consideration of the results of its' children.
     *
     * @param node the node having been walked
     * @param results the results of walking the node's children
     * @return the result of walking the node and it's children
     */
    Object[] leave(IdentityLookup node, Object[] results) throws Exception;

    /**
     * This method is called before walking each child of the argument
     * <code>node</code>. The return value of this method determines if
     * the next child of the argument <code>node</code> should be walked.
     *  The argument <code>resultOfPreviousChild</code>
     * holds the result of walking the previous child of the argument
     * <code>node</code>. Usually, it is used to determine the return value
     * of this method. The argument <code>indexOfNextChild</code>
     * determines the index of the next child to be walked. This index
     * determines the position in the children array of the argument <code>node</code>.
     * Note: The index of the first child is 0.
     *
     * @param node                  the parent node of the children currently walked
     * @param resultOfPreviousChild the result of walking the node's previous child
     * @param indexOfNextChild      the index of the next child to be walked
     * @return <code>false</code>, if no more childs should be walked, else <code>true</code>
     */
    boolean walkNextChild(IdentityLookup node, Object child, Object resultOfPreviousChild, int indexOfNextChild);
    
    /**
     * This method is called before walking any children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to perform any initialization tasks it needs for walking the node's
     * children.
     *
     * @param node the node to be walked
     */
    void arrive(ExecutionEnvironment node) throws Exception;

    /**
     * This method is called after walking the children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to compute the result of walking the argument <code>node</code>
     * and it's children. This result is returned by the tree walker's
     * <code>walk</code> method. The argument <code>results</code> holds
     * the results of walking the children of the argument <code>node</code>.
     * Usually, the result of the argument <code>node</code> is computed in
     * consideration of the results of its' children.
     *
     * @param node the node having been walked
     * @param results the results of walking the node's children
     * @return the result of walking the node and it's children
     */
    Object[] leave(ExecutionEnvironment node, Object[] results) throws Exception;

    /**
     * This method is called before walking each child of the argument
     * <code>node</code>. The return value of this method determines if
     * the next child of the argument <code>node</code> should be walked.
     *  The argument <code>resultOfPreviousChild</code>
     * holds the result of walking the previous child of the argument
     * <code>node</code>. Usually, it is used to determine the return value
     * of this method. The argument <code>indexOfNextChild</code>
     * determines the index of the next child to be walked. This index
     * determines the position in the children array of the argument <code>node</code>.
     * Note: The index of the first child is 0.
     *
     * @param node                  the parent node of the children currently walked
     * @param resultOfPreviousChild the result of walking the node's previous child
     * @param indexOfNextChild      the index of the next child to be walked
     * @return <code>false</code>, if no more childs should be walked, else <code>true</code>
     */
    boolean walkNextChild(ExecutionEnvironment node, Object child, Object resultOfPreviousChild, int indexOfNextChild);
    
    /**
     * This method is called before walking any children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to perform any initialization tasks it needs for walking the node's
     * children.
     *
     * @param node the node to be walked
     */
    void arrive(Activation node) throws Exception;

    /**
     * This method is called after walking the children of the argument
     * <code>node</code>. A node visitor instance uses this method
     * to compute the result of walking the argument <code>node</code>
     * and it's children. This result is returned by the tree walker's
     * <code>walk</code> method. The argument <code>results</code> holds
     * the results of walking the children of the argument <code>node</code>.
     * Usually, the result of the argument <code>node</code> is computed in
     * consideration of the results of its' children.
     *
     * @param node the node having been walked
     * @param results the results of walking the node's children
     * @return the result of walking the node and it's children
     */
    Object[] leave(Activation node, Object[] results) throws Exception;

    /**
     * This method is called before walking each child of the argument
     * <code>node</code>. The return value of this method determines if
     * the next child of the argument <code>node</code> should be walked.
     *  The argument <code>resultOfPreviousChild</code>
     * holds the result of walking the previous child of the argument
     * <code>node</code>. Usually, it is used to determine the return value
     * of this method. The argument <code>indexOfNextChild</code>
     * determines the index of the next child to be walked. This index
     * determines the position in the children array of the argument <code>node</code>.
     * Note: The index of the first child is 0.
     *
     * @param node                  the parent node of the children currently walked
     * @param resultOfPreviousChild the result of walking the node's previous child
     * @param indexOfNextChild      the index of the next child to be walked
     * @return <code>false</code>, if no more childs should be walked, else <code>true</code>
     */
    boolean walkNextChild(Activation node, Object child, Object resultOfPreviousChild, int indexOfNextChild);


}