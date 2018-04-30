package de.julielab.jcore.types;

/* First created by JCasGen Sun Apr 29 15:18:50 CEST 2018 */

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.DocumentAnnotation_Type;

/** Topics label documents with vectors weights for their semantically most prominent words
 * Updated by JCasGen Sun Apr 29 15:57:03 CEST 2018
 * @generated */
public class Topics_Type extends DocumentAnnotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Topics.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("Topics");
 
  /** @generated */
  final Feature casFeat_Weights;
  /** @generated */
  final int     casFeatCode_Weights;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getWeights(int addr) {
        if (featOkTst && casFeat_Weights == null)
      jcas.throwFeatMissing("Weights", "Topics");
    return ll_cas.ll_getRefValue(addr, casFeatCode_Weights);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setWeights(int addr, int v) {
        if (featOkTst && casFeat_Weights == null)
      jcas.throwFeatMissing("Weights", "Topics");
    ll_cas.ll_setRefValue(addr, casFeatCode_Weights, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public double getWeights(int addr, int i) {
        if (featOkTst && casFeat_Weights == null)
      jcas.throwFeatMissing("Weights", "Topics");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_Weights), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_Weights), i);
  return ll_cas.ll_getDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_Weights), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setWeights(int addr, int i, double v) {
        if (featOkTst && casFeat_Weights == null)
      jcas.throwFeatMissing("Weights", "Topics");
    if (lowLevelTypeChecks)
      ll_cas.ll_setDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_Weights), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_Weights), i);
    ll_cas.ll_setDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_Weights), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_IDs;
  /** @generated */
  final int     casFeatCode_IDs;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getIDs(int addr) {
        if (featOkTst && casFeat_IDs == null)
      jcas.throwFeatMissing("IDs", "Topics");
    return ll_cas.ll_getRefValue(addr, casFeatCode_IDs);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setIDs(int addr, int v) {
        if (featOkTst && casFeat_IDs == null)
      jcas.throwFeatMissing("IDs", "Topics");
    ll_cas.ll_setRefValue(addr, casFeatCode_IDs, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public int getIDs(int addr, int i) {
        if (featOkTst && casFeat_IDs == null)
      jcas.throwFeatMissing("IDs", "Topics");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getIntArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_IDs), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_IDs), i);
  return ll_cas.ll_getIntArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_IDs), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setIDs(int addr, int i, int v) {
        if (featOkTst && casFeat_IDs == null)
      jcas.throwFeatMissing("IDs", "Topics");
    if (lowLevelTypeChecks)
      ll_cas.ll_setIntArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_IDs), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_IDs), i);
    ll_cas.ll_setIntArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_IDs), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_ModelID;
  /** @generated */
  final int     casFeatCode_ModelID;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getModelID(int addr) {
        if (featOkTst && casFeat_ModelID == null)
      jcas.throwFeatMissing("ModelID", "Topics");
    return ll_cas.ll_getStringValue(addr, casFeatCode_ModelID);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setModelID(int addr, String v) {
        if (featOkTst && casFeat_ModelID == null)
      jcas.throwFeatMissing("ModelID", "Topics");
    ll_cas.ll_setStringValue(addr, casFeatCode_ModelID, v);}
    
  
 
  /** @generated */
  final Feature casFeat_TopicWords;
  /** @generated */
  final int     casFeatCode_TopicWords;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getTopicWords(int addr) {
        if (featOkTst && casFeat_TopicWords == null)
      jcas.throwFeatMissing("TopicWords", "Topics");
    return ll_cas.ll_getRefValue(addr, casFeatCode_TopicWords);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTopicWords(int addr, int v) {
        if (featOkTst && casFeat_TopicWords == null)
      jcas.throwFeatMissing("TopicWords", "Topics");
    ll_cas.ll_setRefValue(addr, casFeatCode_TopicWords, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getTopicWords(int addr, int i) {
        if (featOkTst && casFeat_TopicWords == null)
      jcas.throwFeatMissing("TopicWords", "Topics");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_TopicWords), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_TopicWords), i);
  return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_TopicWords), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setTopicWords(int addr, int i, String v) {
        if (featOkTst && casFeat_TopicWords == null)
      jcas.throwFeatMissing("TopicWords", "Topics");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_TopicWords), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_TopicWords), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_TopicWords), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Topics_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_Weights = jcas.getRequiredFeatureDE(casType, "Weights", "uima.cas.DoubleArray", featOkTst);
    casFeatCode_Weights  = (null == casFeat_Weights) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Weights).getCode();

 
    casFeat_IDs = jcas.getRequiredFeatureDE(casType, "IDs", "uima.cas.IntegerArray", featOkTst);
    casFeatCode_IDs  = (null == casFeat_IDs) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_IDs).getCode();

 
    casFeat_ModelID = jcas.getRequiredFeatureDE(casType, "ModelID", "uima.cas.String", featOkTst);
    casFeatCode_ModelID  = (null == casFeat_ModelID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_ModelID).getCode();

 
    casFeat_TopicWords = jcas.getRequiredFeatureDE(casType, "TopicWords", "uima.cas.StringArray", featOkTst);
    casFeatCode_TopicWords  = (null == casFeat_TopicWords) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_TopicWords).getCode();

  }
}



    