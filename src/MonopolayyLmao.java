import java.util.ArrayList;

public class MonopolayyLmao implements Bot {
	
	// The public API of YourTeamName must not change
	// You cannot change any other classes
	// YourTeamName may not alter the state of the board or the player objects
	// It may only inspect the state of the board and the player objects
	Board board; 
	Player player;
	Dice dice;
	
	boolean isStartOfTurn, tryBuy, tryBuild, checkBalance;
	int mortgagedProperties;
	String latestProperty="";
	
	MonopolayyLmao (BoardAPI board, PlayerAPI player, DiceAPI dice) {
		this.board = (Board) board;
		this.player = (Player) player;
		this.dice = (Dice) dice;
		isStartOfTurn = true;
		tryBuy=true;
		tryBuild=true;
		checkBalance=true;
		mortgagedProperties=0;
		return;
	}
	
	public String getName () {
		return "MonopolayyLmao";
	}
	
	public String getCommand () {
		//delays commands
		
		try {
		    Thread.sleep(100);                
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
		
		//if out of money...
		if (player.getBalance()<=0){
			if (player.getAssets()>0){
				ArrayList<Property> properties = player.getProperties();
				//first try removing buildings
				if(player.getNumHousesOwned()>0 || player.getNumHotelsOwned()>0){
					for(int i=properties.size()-1;i>=0;i--){
						Property property = properties.get(i);
						String nameOfProperty = property.getName();
						nameOfProperty = getShortName(nameOfProperty);
						if(property instanceof Site){
							Site site = (Site) property;
							if(site.canDemolish(1)){
								return "demolish "+nameOfProperty+ " 1"; 
							}
						}
					}
				}
				
				//then try mortgaging
				for(int i=properties.size()-1;i>=0;i--){
					Property property = properties.get(i);
					if(!property.isMortgaged()){
						String nameOfProperty = property.getName();
						nameOfProperty = getShortName(nameOfProperty);
						mortgagedProperties++;
						return "mortgage "+nameOfProperty;
					}
				}
				//then give up
			}
			return "bankrupt";
		}
		else if (mortgagedProperties>0 && player.getBalance()>210){
			ArrayList<Property> properties = player.getProperties();
			for(int i=properties.size()-1;i>=0;i--){
				Property property = properties.get(i);
				if(property.isMortgaged()){
					String nameOfProperty = property.getName();
					nameOfProperty = getShortName(nameOfProperty);
					mortgagedProperties++;
					return "redeem "+nameOfProperty;
				}
			}
		}
		//whether in jail or not, start turn by rolling
		if ((player.isInJail())&&(isStartOfTurn)){
			isStartOfTurn = false;
			if (player.hasGetOutOfJailCard()){
				
				return "card";
			}else{
				return "roll";
			}
		}if(checkBalance){
				checkBalance=false;
				return "balance";
			}
		if(isStartOfTurn){
			isStartOfTurn = false;
			return "roll";
		}
		if((tryBuy)&&(player.getBalance()>500)&&(board.isProperty(player.getPosition()))){
			tryBuy=false;
			return "buy";
				
			//blunt instrument at the moment, will be made precise
				//
		}
		if ((tryBuild)&&(player.getBalance()>=200)&&(board.isProperty(player.getPosition()))){
			tryBuild=false;
			String property=board.getProperty(player.getPosition()).toString();
			property=getShortName(property);
			int num = 0;
			if (player.getBalance()<400){
				num=1;
			}
			if ((player.getBalance()>400)&&(player.getBalance()<600)){
				num=2;
			}
			if ((player.getBalance()>600)&&(player.getBalance()<800)){
				num=3;
			}
			if ((player.getBalance()>800)&&(player.getBalance()<1000)){
				num=4;
			}
			if ((player.getBalance()>1000)){
				num=5;
			}
			return "build "+property+" "+num;
		}
		checkBalance=true;
		tryBuild=true;
		tryBuy=true;
		isStartOfTurn = true;
		return "done";
		
		
	}
	
	
	public String getDecision () {
		if(player.getBalance()>10) return "pay";
		else return "chance";
	}
	
	private String getShortName(String longName){
		String shortName = "";
		if (longName.equals("Old Kent Rd")){
			shortName="kent";
		}else if (longName.equals("Pall Mall")){
			shortName="mall";
		}else if (longName.equals("King's Cross Station")){
			shortName="kings";
		}else if (longName.equals("The Angel Islington")){
			shortName="angel";
		}
		else{
			String arr[] = longName.split(" ", 2);
			shortName=arr[0].toLowerCase();
		}
		return shortName;
	}
	
	private boolean checkCurrProperty(String propertyName){
		boolean propertyOwned;
		String propertyList = player.getProperties().toString();
		if (propertyList.contains(propertyName)) propertyOwned=true;
		else propertyOwned=false;
		
		return propertyOwned;
	}
	
	/**
	 * checks what squares are in front of the player, and calculates the chance of landing on 
	 * a square which would bring this player below £0
	 * @return double from 0-1, represents a probability 
	 */
	private double getProbabilityOfDebtNextRoll(){
		int currPosition = player.getPosition();
		//probabilities of landing in the spaces 2-12 in front of the player
		//will be set to 0 if they are not owned/have insufficient rent
		double probabilities[] = {1/36, 2/36, 3/36, 4/36, 5/36, 6/36, 5/36, 4/36, 3/36, 2/36, 1/36};
		
		//check the relevant squares in front of the player
		int j = (currPosition+2)%40;
		for(int i=0;i<11;i++){
			//if it's not a property, then no chance of having to pay rent
			if(!board.isProperty(j)){
				probabilities[i]=0;
			}
			//if it is a property...
			else{
				Property property = board.getProperty(j);
				//only have to worry about rent if the property is owned and exceeds current balance
				//and GO is not coming up 
				//(if GO is coming up, then j is after GO, and a lower position than the current one)
				if(!property.isOwned() || !(property.getRent()>player.getBalance()) || j<currPosition){
					probabilities[i]=0;
				}
			}
			j = (j+1)%40;
		}
		int probability = 0;
		for(int i = 0;i<probabilities.length;i++){
			probability += probabilities[i];
		}
		return probability;
	}
	
}
