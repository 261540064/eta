package ghcvm.runtime.closure;

import ghcvm.runtime.*;
import ghcvm.runtime.types.*;
import ghcvm.runtime.message.*;
import static ghcvm.runtime.types.StgTSO.WhyBlocked.*;

public class StgInd extends StgClosure {
    StgClosure indirectee;
    boolean blackhole;

    public StgInd(StgClosure indirectee) {
        this.indirectee = indirectee;
    }

    @Override
    public void enter(StgContext context) {
        while (true) {
            if (!(indirectee instanceof StgInd)) {
                if (indirectee instanceof Object /* StgEvaluating */) {
                    StgTSO currentTSO = context.currentTSO;
                    MessageBlackHole msg = new MessageBlackHole(currentTSO, this);
                    boolean result = context.myCapability.messageBlackHole(msg);
                    if (result) {
                        currentTSO.whyBlocked = BlockedOnBlackHole;
                        currentTSO.blockInfo.setInfo(msg);
                        context.R1 = this;
                        Stg.block_blackhole.enter(context);
                        break;
                    }
                } else {
                    indirectee.enter(context);
                    break;
                }
            }
        }
    }
}