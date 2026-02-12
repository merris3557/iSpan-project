<script >





</script>

<template>
<h1>庫存CRUD</h1>


</template>


<style>
/* Content Card */
.content-card {
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    padding: 2rem;
}

    .card-header {
        padding: 1.5rem;
        border-bottom: 1px solid #e5e7eb;
        display: flex;
        justify-content: space-between;
        align-items: center;

    .card-title {
        font-size: 1.25rem;
        font-weight: 600;
        color: #1f2937;
        margin: 0;
    }

    .view-all-link {
        color: #1e3c72;
        text-decoration: none;
        font-size: 0.875rem;
        font-weight: 500;
        display: flex;
        align-items: center;
        gap: 0.25rem;
        transition: color 0.2s ease;

        &:hover {
            color: #2a5298;
        }
    }
}


/* Table Styles */
.table-responsive {
    overflow-x: auto;
}

.data-table {
    width: 100%;
    border-collapse: collapse;

    thead {
        background-color: #f9fafb;

    th {
        padding: 1rem 1.5rem;
        text-align: left;
        font-size: 0.875rem;
        font-weight: 600;
        color: #6b7280;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        border-bottom: 2px solid #e5e7eb;
        }
    }

    tbody {
        tr {
        border-bottom: 1px solid #e5e7eb;
        transition: background-color 0.2s ease;

        &:hover {
            background-color: #f9fafb;
        }

        &:last-child {
            border-bottom: none;
        }
        }

        td {
        padding: 1rem 1.5rem;
        font-size: 0.875rem;
        color: #374151;

        &.order-id {
            font-family: 'Courier New', monospace;
            font-weight: 600;
            color: #1e3c72;
        }

        &.amount {
            font-weight: 600;
            color: #059669;
        }

        &.date {
            color: #6b7280;
        }
        }
    }
}

.action-buttons {
    display: flex;
    gap: 0.5rem;
}



.btn-action {
    width: 32px;
    height: 32px;
    border: none;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s ease;
    font-size: 0.875rem;

    &.btn-view {
        background-color: #e0e7ff;
        color: #4338ca;

        &:hover {
        background-color: #c7d2fe;
        }
    }

    &.btn-edit {
        background-color: #dbeafe;
        color: #1e40af;

        &:hover {
        background-color: #bfdbfe;
        }
    }
}
</style>