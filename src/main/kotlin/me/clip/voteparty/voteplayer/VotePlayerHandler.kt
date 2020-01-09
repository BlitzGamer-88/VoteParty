package me.clip.voteparty.voteplayer

import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.State
import me.clip.voteparty.database.impl.DatabaseVotePlayerGson
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.OfflinePlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import java.util.UUID

/**
 *
 * And I quote. "Wait till someone complains" - Glare 2020
 *
 */
class VotePlayerHandler(override val plugin: VotePartyPlugin) : Addon, State
{
	
	private val database = DatabaseVotePlayerGson(plugin)
	private val cached = mutableMapOf<Any, VotePlayer>()
	
	
	override fun load()
	{
		database.load()
		
		database.loadBulk(emptyList()).forEach()
		{ (_, data) ->
			data ?: return@forEach
			
			cached[data.uuid] = data
			cached[data.name.toLowerCase()] = data
		}
	}
	
	override fun kill()
	{
		database.saveBulk(cached.values.distinct())
		
		database.kill()
		
		cached.clear()
	}
	
	
	operator fun get(uuid: UUID): VotePlayer?
	{
		return cached[uuid]
	}
	
	operator fun get(name: String): VotePlayer?
	{
		return cached[name.toLowerCase()]
	}
	
	operator fun get(player: OfflinePlayer): VotePlayer?
	{
		return get(player.uniqueId)
	}
	
	
	@EventHandler
	fun PlayerJoinEvent.onJoin()
	{
		val old = cached[player.uniqueId]
		
		if (old != null && old.name != player.name)
		{
			cached -= old.name.toLowerCase()
			cached[player.name.toLowerCase()] = old
		}
		
		if (old == null)
		{
			val new = VotePlayer(player.uniqueId, player.name, mutableListOf())
			
			cached[new.uuid] = new
			cached[new.name.toLowerCase()] = new
		}
	}
	
}