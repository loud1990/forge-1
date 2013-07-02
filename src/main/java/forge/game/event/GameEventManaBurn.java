package forge.game.event;

// This special event denotes loss of mana due to phase end
public class GameEventManaBurn extends GameEvent {
    
    public final boolean causedLifeLoss;
    public final int amount;
    
    /**
     * TODO: Write javadoc for Constructor.
     * @param dealDamage 
     * @param burn 
     */
    public GameEventManaBurn(int burn, boolean dealDamage) {
        amount = burn;
        causedLifeLoss = dealDamage;
    }
    
    @Override
    public <T> T visit(IGameEventVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
