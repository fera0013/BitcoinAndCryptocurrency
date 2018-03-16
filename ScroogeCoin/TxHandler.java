public class TxHandler {
	private UTXOPool utxoPool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
    	this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
    	java.util.List<UTXO> claimedUtxos = new java.util.ArrayList<UTXO>();
    	double inputSum =0;
        double outputSum = 0;
    	for(int i=0;i<tx.numInputs();i++)
    	{
    		Transaction.Input input = tx.getInput(i);
    		UTXO claimedUtxo = new UTXO(input.prevTxHash,input.outputIndex);
    		//(1) all outputs claimed by {@code tx} are in the current UTXO pool
    		if(!this.utxoPool.contains(claimedUtxo)) return false;
    		//(2) the signatures on each input of {@code tx} are valid
    		Transaction.Output claimedOutput = utxoPool.getTxOutput(claimedUtxo);
    		if (!Crypto.verifySignature(claimedOutput.address, tx.getRawDataToSign(i), input.signature)) return false;
    		//(3) no UTXO is claimed multiple times by {@code tx}
    		if(claimedUtxos.contains(claimedUtxo))
    		{
    			return false;
    		}
    		else
    		{
    			claimedUtxos.add(claimedUtxo);
    		}
    		inputSum+=claimedOutput.value;	
    	}
    	for(Transaction.Output output : tx.getOutputs())
    	{
    		//(4) all of {@code tx}s output values are non-negative
    		if(output.value<0) return false;
    		outputSum += output.value;
    	}
    	//(5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
    	if(inputSum<outputSum) return false;
    	return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
    	  java.util.Set<Transaction> validTxs = new java.util.HashSet<>();
          for (Transaction tx : possibleTxs) {
              if (isValidTx(tx)) {
                  validTxs.add(tx);
                  for (Transaction.Input in : tx.getInputs()) {
                      UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
                      utxoPool.removeUTXO(utxo);
                  }
                  for (int i = 0; i < tx.numOutputs(); i++) {
                      Transaction.Output out = tx.getOutput(i);
                      UTXO utxo = new UTXO(tx.getHash(), i);
                      utxoPool.addUTXO(utxo, out);
                  }
              }
          }

          Transaction[] validTxArray = new Transaction[validTxs.size()];
          return validTxs.toArray(validTxArray);
      }
    }

