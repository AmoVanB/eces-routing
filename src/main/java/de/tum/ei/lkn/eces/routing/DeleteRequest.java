package de.tum.ei.lkn.eces.routing;

import de.tum.ei.lkn.eces.core.Component;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;

/**
 * Component telling the Routing System to delete a Request.
 *
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = RoutingSystem.class)
public class DeleteRequest extends Component {
    private Long entityToDelete;
    public DeleteRequest(Long entityId) {
        this.entityToDelete = entityId;
    }

    public Long getEntityToDelete() {
        return this.entityToDelete;
    }
}
